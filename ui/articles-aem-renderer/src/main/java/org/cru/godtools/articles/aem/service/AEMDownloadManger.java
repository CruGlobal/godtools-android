package org.cru.godtools.articles.aem.service;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.WorkerThread;

import org.apache.commons.io.IOUtils;
import org.cru.godtools.articles.aem.db.ArticleRepository;
import org.cru.godtools.articles.aem.db.AttachmentRepository;
import org.cru.godtools.articles.aem.db.ManifestAssociationRepository;
import org.cru.godtools.articles.aem.model.Article;
import org.cru.godtools.articles.aem.model.Attachment;
import org.cru.godtools.articles.aem.model.ManifestAssociation;
import org.cru.godtools.articles.aem.service.support.ArticleParser;
import org.cru.godtools.xml.model.Manifest;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

/**
 * This class hold all Download methods for retrieving and saving an Article
 *
 * @author Gyasi Story
 */
public class AEMDownloadManger {

    /**
     * This method  handles loading AEM Imports into the local Database.  Please ensure that the
     * manifest used contains AemImports.
     * @param manifest = the model for the manifest
     * @param context = the Application Context
     * @throws URISyntaxException =
     * @throws JSONException =
     * @throws IOException =
     */
    @WorkerThread
    public static void loadAEMFromManifest(final Manifest manifest, Context context)
            throws URISyntaxException, JSONException, IOException {

        // Verify that Manifest has AEM Articles (Should be checked already
        if (manifest.getAemImports() == null || manifest.getAemImports().size() <= 0) {

            return;  // May need to throw an error to limit unnecessary calls.
        }

        for (Uri aemImports : manifest.getAemImports()) {
            loadAemManifestIntoAemModel(manifest, aemImports, context);
        }

    }


    /**
     * This method take the manifest and one of its aemImports and extracts all associated data to
     * the database.
     * @param manifest = manifest object
     * @param aemImports = uri from one of the aemImports
     * @param context = the Application or Activity Context
     * @throws JSONException
     * @throws IOException
     * @throws URISyntaxException
     */
    private static void loadAemManifestIntoAemModel(Manifest manifest, Uri aemImports, Context context)
            throws JSONException, IOException, URISyntaxException {

        ArticleRepository articleRepository = new ArticleRepository(context);
        AttachmentRepository attachmentRepository = new AttachmentRepository(context);
        ManifestAssociationRepository manifestAssociationRepository =
                new ManifestAssociationRepository(context);

        JSONObject importJson = getJsonFromUri(aemImports);

        ArticleParser articleParser = new ArticleParser(importJson);
        HashMap<String, Object> articleResults = articleParser.execute();

        List<Article> articles = (List<Article>) articleResults.get(ArticleParser.ARTICLE_LIST_KEY);

        List<Attachment> attachments = (List<Attachment>) articleResults
                .get(ArticleParser.ATTACHMENT_LIST_KEY);

        for (Article createdArticle : articles) {
            ManifestAssociation createdAssociation = new ManifestAssociation();
            createdAssociation.mManifestId = manifest.getCode();
            createdAssociation.mManifestName = manifest.getManifestName();
            createdAssociation.mArticleId = createdArticle.mkey;

            // Save Association
            manifestAssociationRepository.insertAssociation(createdAssociation);

            // Save Article
            articleRepository.insertArticle(createdArticle);
        }

        for (Attachment createdAttachement : attachments) {
            attachmentRepository.insertAttachment(createdAttachement);
            //Todo: Decide if you want to store attachment here.
        }


    }

    /**
     * Gets JSON Object out of Uri
     *
     * @param aemImports = uri
     * @return = JSON object from the Uri
     * @throws JSONException =
     * @throws URISyntaxException
     * @throws IOException
     */
    private static JSONObject getJsonFromUri(Uri aemImports)
            throws JSONException, URISyntaxException, IOException {

        // Have to convert android Uri to a Java URI
        URI uri = new URI(aemImports.toString());
        String results = IOUtils.toString(uri);
        return new JSONObject(results);
    }

    /**
     * This method is used to save an Attachment to Storage and update Database
     *
     * @param attachment = the attachment to be saved
     * @param context    = The context is needed to save the attachment and database entry
     * @throws IOException = Is thrown if an error occurs in saving to storage.
     */
    public static void saveAttachmentToStorage(Attachment attachment, Context context)
            throws IOException {
        // verify that attachment is not already saved.
        if (attachment.mAttachmentFilePath != null) {
            //TODO: determine what should happen
        } else {
            String[] urlSplit = attachment.mAttachmentUrl.split("/");
            String filename = urlSplit[urlSplit.length - 1];
            File articleFile = new File(context.getFilesDir(), "articles");
            if (!articleFile.exists()) {
                articleFile.mkdir();
            }
            articleFile = new File(articleFile, filename);
            FileOutputStream outputStream = new FileOutputStream(articleFile);
            URL url = new URL(attachment.mAttachmentUrl);
            outputStream.write(IOUtils.toByteArray(url));

            // update attachment with file Path
            attachment.mAttachmentFilePath = articleFile.getAbsolutePath();
            AttachmentRepository repository = new AttachmentRepository(context);
            repository.updateAttachment(attachment);
        }

    }
}
