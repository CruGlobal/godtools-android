#!/usr/bin/env python3
"""Verify that every relative link in wiki/ resolves on the published GitHub Wiki.

publish-wiki.yml mirrors wiki/ to the wiki repository, rewriting inter-page link
targets of the form `Page.md#anchor` to `Page#anchor` and dropping a leading H1
that only repeats the filename-derived title the wiki already renders. That
rewrite silently produces a dead link when the target page has been renamed or
deleted, or when the heading an anchor points at is gone, and the publish job
runs only after merge -- so the check happens here, on pull requests, before the
break can reach the live wiki.

Links are checked in their source form, against wiki/ rather than a transformed
copy, so a failure names the file and line a contributor actually edits.

Markdown at the repository root is checked too, for the opposite direction:
README.md links into wiki/, and renaming or deleting a page breaks those links
just as surely, with nothing else to catch it.
"""

import os
import re
import sys
from collections import Counter

# Matches the target of a markdown inline link, and any title following it. All
# three CommonMark title forms are recognized, so that no link is invisible to
# this check; SED_TITLE decides which of them the publish job can rewrite.
LINK = re.compile(r'\]\(([^)\s]+)(\s+(?:"[^"]*"|\'[^\']*\'|\([^)]*\)))?\)')
# The one title form publish-wiki.yml's sed rewrites, spelled as that sed spells
# it: literal spaces, then a "double-quoted" title. A tab, or a 'single-quoted'
# or (parenthesized) title, leaves the .md extension in place for the job's own
# guard to reject -- after merge, where no pull request check can catch it. Such
# links are rejected here instead, so the failure lands on the pull request.
SED_TITLE = re.compile(r'^ +"[^"]*"$')
# Matches a link reference definition -- `[label]: target "Title"`. The publish
# job's sed rewrites only the inline `](target)` form, and its leftover-.md guard
# only looks for that form, so a page target written this way is neither
# rewritten nor caught, and reaches the wiki dead.
LINK_DEF = re.compile(r"^ {0,3}\[[^\]]+\]:\s*(\S+)")
HEADING = re.compile(r"^(#{1,6})\s+(.*?)\s*$")
CODE_SPAN = re.compile(r"`([^`]*)`")
INLINE_LINK = re.compile(r"\[([^\]]*)\]\([^)]*\)")


def uncoded_lines(path):
    """Yield (lineno, line) outside fenced code blocks, as the publish job does.

    Leading whitespace is ignored when matching a fence, because a code block
    nested in a numbered list is indented. Both fence patterns in
    publish-wiki.yml -- the sed range and the leftover-.md guard -- match fences
    the same way and must stay in sync with this.
    """
    fenced = False
    with open(path, encoding="utf-8") as handle:
        for lineno, line in enumerate(handle, 1):
            if line.lstrip().startswith("```"):
                fenced = not fenced
                continue
            if not fenced:
                yield lineno, line


def is_external(target):
    """Report whether a link target leaves the repository, so nothing here resolves it."""
    return "://" in target or target.startswith("mailto:")


def normalize(text):
    """Compare a heading to a page title the way the publish job's normalize() does."""
    return re.sub(r"[^a-z0-9]", "", text.lower().replace("&", "and"))


def anchors(path, page=None):
    """Return the heading anchors served for a page.

    GitHub slugs a heading by lowercasing it, dropping every character that is
    not a word character, hyphen, or space, turning spaces into hyphens, and
    suffixing repeats with -1, -2, and so on. Pass `page` for the anchors the
    published wiki serves, where a first-line H1 that duplicates the page title
    is left out entirely: the publish job deletes that line, so the wiki neither
    serves its slug nor counts it when numbering repeats. Omit `page` for the
    anchors GitHub serves when the file is rendered in the repository, which
    still has that H1.
    """
    headings = [(lineno, match.group(2)) for lineno, line in uncoded_lines(path) if (match := HEADING.match(line))]
    if page is not None:
        title = normalize(os.path.splitext(page)[0].replace("-", " "))
        if headings and headings[0][0] == 1 and normalize(headings[0][1]) == title:
            headings.pop(0)

    slugs = set()
    seen = Counter()
    for _, heading in headings:
        text = INLINE_LINK.sub(r"\1", CODE_SPAN.sub(r"\1", heading))
        slug = re.sub(r"[^\w\- ]", "", text.lower()).replace(" ", "-")
        repeat = seen[slug]
        seen[slug] += 1
        slugs.add(slug if repeat == 0 else f"{slug}-{repeat}")
    return slugs


def main(root):
    # Normalized so a trailing slash cannot defeat the containment checks below.
    root = os.path.normpath(root)
    # wiki/ sits at the repository root, which is where the pages linking into
    # it live.
    repo = os.path.dirname(root) or "."
    pages = sorted(
        name for name in os.listdir(root) if name.endswith(".md") and os.path.isfile(os.path.join(root, name))
    )
    if not pages:
        print(f"::error::No wiki pages found in {root}/.")
        return 1

    published = {page: anchors(os.path.join(root, page), page) for page in pages}
    errors = []

    def fail(source, lineno, message):
        errors.append(f"::error file={source},line={lineno}::{message}")

    for page in pages:
        source = os.path.join(root, page)
        for lineno, line in uncoded_lines(source):
            # The publish job rewrites and guards inline links only, so a page
            # named by a reference definition keeps the .md extension the wiki
            # does not serve -- dead even when that page exists.
            if match := LINK_DEF.match(line):
                target = match.group(1)
                if not is_external(target) and target.partition("#")[0].endswith(".md"):
                    fail(
                        source,
                        lineno,
                        f"{page} points a link reference definition at {target}. The publish job rewrites only "
                        "inline links, so this would be published as a dead .md link; write it inline as "
                        f"[Text]({target}).",
                    )

            for match in LINK.finditer(line):
                target = match.group(1)
                if is_external(target):
                    continue

                path, _, anchor = target.partition("#")
                if not path:
                    if anchor not in published[page]:
                        fail(source, lineno, f"{page} links to #{anchor}, which is not a heading on this page.")
                    continue

                if path.endswith(".md"):
                    title = match.group(2)
                    if path not in published:
                        fail(source, lineno, f"{page} links to {path}, which is not a page in {root}/.")
                    elif title is not None and not SED_TITLE.match(title):
                        fail(
                            source,
                            lineno,
                            f"{page} links to {path} with a title the publish job cannot rewrite, which would "
                            f'fail the publish after merge. Write the title as [Text]({path} "Title") -- literal '
                            "spaces, double quotes -- or drop it.",
                        )
                    elif anchor and anchor not in published[path]:
                        fail(source, lineno, f"{page} links to {path}#{anchor}, which is not a heading on that page.")
                    continue

                # Not a page, so it must be a file the wiki repository serves
                # alongside them -- an image, for instance.
                asset = os.path.normpath(os.path.join(root, path))
                if not asset.startswith(root + os.sep) or not os.path.isfile(asset):
                    fail(
                        source, lineno, f"{page} links to {target}, which is neither a wiki page nor a file in {root}/."
                    )

    # The other direction: README.md and its siblings link into wiki/, and those
    # links go stale when a page is renamed or deleted. They are followed inside
    # the repository rather than on the wiki, so they keep the .md extension the
    # publish job strips, and they resolve against the file as GitHub renders it
    # here -- leading H1 and all.
    in_repo = {page: anchors(os.path.join(root, page)) for page in pages}
    inbound = sorted(
        name for name in os.listdir(repo) if name.endswith(".md") and os.path.isfile(os.path.join(repo, name))
    )

    for name in inbound:
        source = os.path.normpath(os.path.join(repo, name))
        for lineno, line in uncoded_lines(source):
            # Reference definitions count here too. Nothing rewrites a
            # repository file, so both link forms resolve the same way and go
            # stale the same way when a page is renamed -- unlike in wiki/,
            # there is nothing about the reference form to reject.
            targets = [match.group(1) for match in LINK.finditer(line)]
            if match := LINK_DEF.match(line):
                targets.append(match.group(1))

            for target in targets:
                if is_external(target):
                    continue

                path, _, anchor = target.partition("#")
                if not path.endswith(".md"):
                    continue

                # Only links that land inside wiki/ are this script's business;
                # anything else is an ordinary repository link.
                page = os.path.normpath(os.path.join(repo, path))
                if not page.startswith(root + os.sep):
                    continue
                page = page[len(root) + 1 :]

                if page not in in_repo:
                    fail(source, lineno, f"{name} links to {target}, which is not a page in {root}/.")
                elif anchor and anchor not in in_repo[page]:
                    fail(source, lineno, f"{name} links to {target}, which is not a heading on that page.")

    for error in errors:
        print(error)
    if errors:
        print(
            f"::error::{len(errors)} wiki link(s) do not resolve. Link a wiki page as [Text](Page.md) or "
            "[Text](Page.md#heading-anchor) using a filename from wiki/; link anything else by full https:// URL."
        )
        return 1

    print(
        f"Checked {len(pages)} wiki pages and {len(inbound)} repository markdown file(s); "
        "all relative links and anchors resolve."
    )
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1] if len(sys.argv) > 1 else "wiki"))
