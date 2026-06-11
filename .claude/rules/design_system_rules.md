# Design System Rules for Jetpack Compose

This document defines the design system conventions for GodTools Android to ensure consistency when writing Jetpack Compose UI code.

The shared design tokens live in `ui/base` (`GodToolsTheme.kt`, `Color.kt`) and are surfaced through Material3's `MaterialTheme.*` API plus `GodToolsTheme.extendedColorScheme` for brand-specific colors that have no M3 equivalent.

---

## 1. Technology Stack

| Layer | Technology |
|---|---|
| UI framework | **Jetpack Compose** (Android) |
| Component library | **Material3** via `androidx.compose.material3` |
| Iconography | Android vector drawables (`res/drawable/`) using `@color/tintable` fill |
| Theme entry point | `GodToolsTheme(content)` composable in `:ui:base` |
| Brand color generation | [Material 3 theme builder](https://m3.material.io/theme-builder) — Primary `#3BA4DB`, Secondary `#3BA4DB`, Neutral `#8F9193` |
| Resources | Standard Android resources under `res/` |

Feature modules consume the theme by being wrapped in `GodToolsTheme { … }` at the app root and then reading `MaterialTheme.colorScheme.*`, `MaterialTheme.typography.*`, `MaterialTheme.shapes.*`, and `GodToolsTheme.extendedColorScheme.*` inside any `@Composable`.

---

## 2. Theme Entry Point — `GodToolsTheme`

```kotlin
@Composable
fun GodToolsTheme(
    darkTheme: Boolean = isSystemInDarkTheme() && BuildConfig.DEBUG,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
)
```

- Apply `GodToolsTheme` exactly **once**, at the app root. Feature composables must NOT call `MaterialTheme(...)` or `GodToolsTheme(...)` themselves.
- Dark mode is currently restricted to debug builds (`BuildConfig.DEBUG`). Do not assume dark mode is available in production.
- Inside any `@Composable`, the active light-mode state is exposed via:
  ```kotlin
  val isLight = GodToolsTheme.isLightColorSchemeActive
  ```
  Use this only when a composable needs to branch beyond what `MaterialTheme.colorScheme` already handles (e.g. choosing between primary-colored and default app bar colors).
- Extended brand colors not covered by Material3 are available via:
  ```kotlin
  val extended = GodToolsTheme.extendedColorScheme
  // extended.green.color / .onColor / .colorContainer / .onColorContainer
  // extended.red.color / .onColor / .colorContainer / .onColorContainer
  ```

---

## 3. Color Tokens

This project uses Material3's full **`ColorScheme`** as the source of truth. Map Figma color variables to these tokens — do NOT introduce ad-hoc `Color(0xFF…)` literals in feature composables.

### Primary palette (light values; dark variants exist for every token)

| Figma intent | Token | Value (light) |
|---|---|---|
| Primary brand (GT Blue) | `MaterialTheme.colorScheme.primary` | `#3BA4DB` (overridden to `#216487` by M3 generator) |
| On-primary text/icon | `MaterialTheme.colorScheme.onPrimary` | `#FFFFFF` |
| Primary container background | `MaterialTheme.colorScheme.primaryContainer` | `#C7E7FF` |
| On-primary-container text/icon | `MaterialTheme.colorScheme.onPrimaryContainer` | `#004C6C` |
| Secondary brand | `MaterialTheme.colorScheme.secondary` | `#216487` |
| Secondary container | `MaterialTheme.colorScheme.secondaryContainer` | `#C7E7FF` |
| Error | `MaterialTheme.colorScheme.error` | `#BA1A1A` |
| Error container | `MaterialTheme.colorScheme.errorContainer` | `#FFDAD6` |

> **Note:** The raw GT Blue (`GodToolsTheme.GT_BLUE = #3BA4DB`) and GT Red (`GodToolsTheme.GT_RED = #E55B36`) constants are available for contexts that need the exact brand color (e.g. a logo or illustration tint). Prefer the M3 token equivalents for all UI chrome.

### Extended colors (brand-specific, no M3 equivalent)

Access via `GodToolsTheme.extendedColorScheme`:

| Figma intent | Token |
|---|---|
| Success / positive state | `extendedColorScheme.green.color` / `.onColor` / `.colorContainer` / `.onColorContainer` |
| Warning / destructive state | `extendedColorScheme.red.color` / `.onColor` / `.colorContainer` / `.onColorContainer` |

### Surface palette

| Figma intent | Token |
|---|---|
| App background | `MaterialTheme.colorScheme.background` / `surface` |
| Card / sheet background | `MaterialTheme.colorScheme.surface` |
| Card variant / muted background | `MaterialTheme.colorScheme.surfaceVariant` |
| Lowest tonal surface | `MaterialTheme.colorScheme.surfaceContainerLowest` |
| Low tonal surface | `MaterialTheme.colorScheme.surfaceContainerLow` |
| Default tonal surface | `MaterialTheme.colorScheme.surfaceContainer` |
| High tonal surface | `MaterialTheme.colorScheme.surfaceContainerHigh` |
| Highest tonal surface | `MaterialTheme.colorScheme.surfaceContainerHighest` |
| Outline (borders, dividers) | `MaterialTheme.colorScheme.outline` |
| Outline variant (subtle dividers) | `MaterialTheme.colorScheme.outlineVariant` |
| Inverse surface (snackbars) | `MaterialTheme.colorScheme.inverseSurface` |
| Scrim (modal background) | `MaterialTheme.colorScheme.scrim` |

> **Surface tint is disabled** in GodToolsTheme (`surfaceTint = Color.White`). Tonal elevation does not produce a visible color shift on surfaces — use explicit `surfaceContainer*` tokens for visual hierarchy instead.

### Adding new colors

1. **First option (preferred):** find the closest semantic M3 token. `surfaceContainerHigh`, `outlineVariant`, and `extendedColorScheme.green/red` cover most cases.
2. **Second option:** if a genuinely new brand color is needed, add it as a `ColorFamily` to `ExtendedColorScheme` in `GodToolsTheme.kt` and `Color.kt`. Document why no existing token fits.
3. **Never** use a `Color(0xFF…)` literal inline in a feature composable. The only acceptable inline `Color` values are `Color.Transparent` and `Color.Unspecified` (used as sentinel values in Modifier APIs).

---

## 4. Typography

GodToolsTheme uses Material3's default `Typography` with one override: `titleMedium.lineHeight = 22.sp`. Use `MaterialTheme.typography` tokens:

| Figma intent | Token |
|---|---|
| Screen titles | `MaterialTheme.typography.titleLarge` |
| Section titles | `MaterialTheme.typography.titleMedium` |
| Body text | `MaterialTheme.typography.bodyLarge` / `bodyMedium` |
| Captions / labels | `MaterialTheme.typography.bodySmall` / `labelMedium` |
| Button text | `MaterialTheme.typography.labelLarge` |

```kotlin
Text(
    text = "Page Title",
    style = MaterialTheme.typography.titleLarge,
    color = MaterialTheme.colorScheme.onSurface,
)
```

**Never hardcode `fontSize = 16.sp` or `fontWeight = FontWeight.Bold` on a `Text` composable** when a typography token captures the same intent.

---

## 5. Spacing

Material3 uses a **4dp base grid**.

| Value | Common usage |
|---|---|
| `4.dp` | Between an icon and inline label |
| `8.dp` | Between adjacent inline elements |
| `12.dp` | Inside compact components |
| `16.dp` | Screen padding, between card sections |
| `24.dp` | Between unrelated sections |
| `32.dp` | Top of screen, hero spacing |
| `48.dp` | Minimum touch target / large breaks |

Prefer `Arrangement.spacedBy(16.dp)` on `Column`/`Row` over per-element `padding` — it keeps spacing controlled from the parent and easier to reason about. Per-element `Modifier.padding` is appropriate only when a single child needs an offset.

```kotlin
Column(
    verticalArrangement = Arrangement.spacedBy(16.dp),
    modifier = Modifier.fillMaxWidth().padding(16.dp),
) {
    Text("Title", style = MaterialTheme.typography.titleLarge)
    Text("Body", style = MaterialTheme.typography.bodyMedium)
}
```

---

## 6. Elevation

Surface tint is disabled in GodToolsTheme — tonal elevation produces no visible color shift. Use explicit `surfaceContainer*` tokens for hierarchy instead of bumping `shadowElevation`.

For standard components, use `CardDefaults` and `TopAppBarDefaults` rather than supplying raw `dp` values — they respond to the active color scheme correctly.

---

## 7. Shape

Access via `MaterialTheme.shapes`:

| Figma intent | Token | Default radius |
|---|---|---|
| Extra small (chips) | `MaterialTheme.shapes.extraSmall` | 4.dp |
| Small (buttons) | `MaterialTheme.shapes.small` | 8.dp |
| Medium (cards) | `MaterialTheme.shapes.medium` | 12.dp |
| Large (sheets, dialogs) | `MaterialTheme.shapes.large` | 16.dp |
| Extra large (hero) | `MaterialTheme.shapes.extraLarge` | 28.dp |
| Circular (avatars) | `CircleShape` | — |

Avoid hand-crafting `RoundedCornerShape(8.dp)` when a token covers the same intent — the token survives theme overrides.

---

## 8. Component Library (Material3)

Always prefer the Material3 component over building from primitives when one exists.

### Buttons

```kotlin
Button(onClick = { state.eventSink(UiEvent.Save) }) { Text("Save") }
FilledTonalButton(onClick = { … }) { Text("Secondary action") }
OutlinedButton(onClick = { … }) { Text("Cancel") }
TextButton(onClick = { … }) { Text("Learn more") }
IconButton(onClick = { … }) { Icon(painter = painterResource(R.drawable.ic_close), contentDescription = "Close") }
```

### App bar

Use `GodToolsTheme.topAppBarColors` for the correct brand colors in light/dark mode:

```kotlin
TopAppBar(
    title = { Text("Screen Title") },
    navigationIcon = { IconButton(onClick = { … }) { Icon(…) } },
    colors = GodToolsTheme.topAppBarColors,
)
```

### Cards

```kotlin
Card(modifier = Modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Title", style = MaterialTheme.typography.titleMedium)
        Text("Body", style = MaterialTheme.typography.bodyMedium)
    }
}
```

### Search bar

Use `GodToolsTheme.searchBarColors` for the correct container color in light/dark mode:

```kotlin
SearchBar(
    colors = GodToolsTheme.searchBarColors,
    …
)
```

### Text fields

```kotlin
OutlinedTextField(
    value = state.query,
    onValueChange = { state.eventSink(UiEvent.QueryChanged(it)) },
    label = { Text(stringResource(R.string.label_search)) },
    singleLine = true,
    modifier = Modifier.fillMaxWidth(),
)
```

### Scaffolding

```kotlin
Scaffold(
    topBar = {
        TopAppBar(
            title = { Text(stringResource(R.string.screen_title)) },
            colors = GodToolsTheme.topAppBarColors,
        )
    },
) { innerPadding ->
    LazyColumn(
        contentPadding = innerPadding,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(state.items, key = { it.id }) { item -> ItemRow(item) }
    }
}
```

---

## 9. Icons

Vector drawables live in `res/drawable/` and follow the `ic_<name>` naming convention. Fill color in the XML must be `@color/tintable` — not a hardcoded color — so that the `Icon` composable can apply tinting via `LocalContentColor` or the `tint` parameter:

```xml
<!-- Correct -->
<path android:fillColor="@color/tintable" … />

<!-- Wrong — hardcoded color ignores tinting -->
<path android:fillColor="#FF3B82F6" … />
```

Pass to `Icon` via `painterResource`:

```kotlin
Icon(
    painter = painterResource(R.drawable.ic_share),
    contentDescription = stringResource(R.string.action_share),
    tint = MaterialTheme.colorScheme.onPrimary,
)
```

**Every `Icon`/`Image` composable MUST have a `contentDescription`.** Pass `null` only if the icon is purely decorative AND there is adjacent text that already conveys the same meaning.

Do NOT hand-roll vector path code inline in a composable file. Vector paths belong in drawable XML resources.

---

## 10. Layout Patterns

### Modifier order

```kotlin
Box(
    modifier = modifier                  // 1. caller-provided (always first)
        .fillMaxWidth()                  // 2. size
        .padding(16.dp)                  // 3. spacing
        .background(MaterialTheme.colorScheme.surfaceVariant)
        .clip(MaterialTheme.shapes.medium)
        .clickable { … }                 // 4. interaction
        .semantics { … },                // 5. accessibility
)
```

- Every public or internal composable MUST accept and forward `modifier: Modifier = Modifier`.

### Lists

```kotlin
LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
) {
    items(state.tools, key = { it.code }) { tool ->
        ToolCard(tool = tool, onClick = { state.eventSink(UiEvent.OpenTool(tool.code)) })
    }
}
```

`key = { … }` is required when the list can reorder or items can be added/removed. Keys derived from nullable fields must handle null safely — do not use `.orEmpty()` on a nullable key (two `null` IDs both become `""` and crash). Either omit `key` or use the object as a fallback: `key = { it.id ?: it }`.

### State hoisting

Stateful UI that needs to be testable lives in the Presenter `UiState`, not in Layout-local `remember { mutableStateOf(…) }`. Layout-local `remember` is acceptable only for transient view-only state (animation progress, password visibility toggle).

---

## 11. Dark Mode

Dark mode is currently **debug-only** (`GodToolsTheme` defaults to `darkTheme = isSystemInDarkTheme() && BuildConfig.DEBUG`).

- Do NOT branch on `GodToolsTheme.isLightColorSchemeActive` for color decisions — use `MaterialTheme.colorScheme.*` tokens, which already carry the correct dark variant.
- DO branch on `isLightColorSchemeActive` for non-color decisions, such as selecting `GodToolsTheme.topAppBarColors` vs. the default (the app bar is primary-colored in light mode, default-colored in dark mode).

---

## 12. Accessibility

### Required

- **Every** `Icon`/`Image` has a meaningful `contentDescription` (or `null` with justification).
- Touch targets are ≥ 48.dp. Use `Modifier.minimumInteractiveComponentSize()` on custom interactive composables, or wrap small icons in `IconButton`.
- Form fields use `KeyboardOptions(imeAction = ImeAction.Next)` to chain through fields and `imeAction = ImeAction.Done` on the last.
- Composables that act as buttons but are not a `Button` use `Modifier.semantics { role = Role.Button }`.
- Heading-like `Text` composables use `Modifier.semantics { heading() }`.

### Testing

Layout tests assert accessibility surface:

```kotlin
onNodeWithContentDescription("Close").assertExists().performClick()
onNode(hasText("Share").and(hasClickAction())).assertExists()
```

---

## 13. Resources

User-visible strings come from Android resources, never inlined as Kotlin literals:

```kotlin
Text(stringResource(R.string.dashboard_title))
```

- Strings live in `res/values/strings.xml`. Keys follow `<screen>_<section>_<purpose>` (e.g., `dashboard_lessons_section_personalized_no_lessons_action_all_lessons`).
- String resource names for user-facing actions use a descriptive suffix for the action (e.g., `_action_all_lessons`), not for the widget type (e.g., not `_button`).
- Drawable assets: `res/drawable/ic_<name>.xml`.
- Do NOT define `app_name`, colors, or themes in library module resources — those belong in `:app`.

---

## 14. Quick Reference — Compose Conventions

1. **Use Material3 components first.** A Figma "card" → `Card`, "button" → `Button`/`FilledTonalButton`/`OutlinedButton`/`TextButton`, "chip" → `FilterChip`/`AssistChip`, "text field" → `OutlinedTextField`. Build from primitives only when no component fits.
2. **Use Material3 semantic colors.** Map Figma color variables to `MaterialTheme.colorScheme.*`. For success/error states use `GodToolsTheme.extendedColorScheme.green`/`.red`. Never use a raw `Color(0xFF…)` literal in a feature composable.
3. **Use Material3 typography tokens.** Map Figma type styles to `MaterialTheme.typography.*`. Don't pass raw `fontSize` or `fontWeight` to a `Text` when a token covers the intent.
4. **Use Material3 shape tokens.** Map Figma corner radii to `MaterialTheme.shapes.*`. Use `CircleShape` for fully circular elements.
5. **Spacing on the 4dp grid.** Map Figma values to the nearest 4dp multiple. Prefer `Arrangement.spacedBy(N.dp)` over per-element padding.
6. **Icons via vector drawables.** Use `res/drawable/ic_<name>.xml` with `@color/tintable` fill. Pass via `painterResource(R.drawable.ic_name)` to `Icon`. Do not hardcode fill colors in drawable XML.
7. **Layouts use Compose primitives.** `Column`, `Row`, `Box`, `LazyColumn`, `LazyRow`, `Scaffold`. Do not use legacy XML view layouts via `AndroidView` for new UI.
8. **Modifier order:** caller's `modifier` first, then size, padding, background, clip, clickable, semantics. Every public composable accepts and forwards `modifier`.
9. **State hoisting:** business state lives in Presenter `UiState`. Layout-local `remember` only for transient view-only state.
10. **Loading / error / empty states are first-class.** Every screen that loads data shows distinct UI for `isLoading`, `error`, and `isEmpty`.
11. **Dark mode tokens are automatic** — using `MaterialTheme.colorScheme.*` consistently means dark mode just works. Branch on `GodToolsTheme.isLightColorSchemeActive` only for non-color decisions.
12. **Accessibility is required.** Every `Icon`/`Image` has `contentDescription`. Touch targets ≥ 48.dp. Custom click handlers carry semantics.
13. **Strings come from resources** (`stringResource(R.string.*)`). Never inline user-visible English in a composable.