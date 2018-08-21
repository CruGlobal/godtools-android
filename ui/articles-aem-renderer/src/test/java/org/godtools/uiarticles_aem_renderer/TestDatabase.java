package org.godtools.uiarticles_aem_renderer;

import android.arch.persistence.room.Room;
import android.content.Context;
//import android.support.test.InstrumentationRegistry;
//import android.support.test.runner.AndroidJUnit4;

import org.godtools.uiarticles_aem_renderer.db.ArticleDao;
import org.godtools.uiarticles_aem_renderer.db.ArticleRoomDatabase;
import org.godtools.uiarticles_aem_renderer.model.Article;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

//@RunWith(AndroidJUnit4.class)
public class TestDatabase {

    private ArticleRoomDatabase mDb;
    private ArticleDao mArticleDao;

    @Before
    public void createDb(){
//        Context context = InstrumentationRegistry.getContext();
//        mDb = Room.inMemoryDatabaseBuilder(context, ArticleRoomDatabase.class).build();
//        mArticleDao = mDb.mArticleDao();
    }

    @After
    public void closeDb() {
        mDb.close();
    }


    @Test
    public void writeToArticleDatabase() {
//        Article article = new Article();
//        article.mContent = "<p>When one is studying a passage of Scripture, a basic but helpful pattern to follow is the threefold process of observation, interpretation, and application.</p>\\n<p>OBSERVATION</p>\\n<p>This is pretty straightforward: observe what the passage is saying and describing.<br>\\nFirst, read through a portion of Scripture and then go back and make some initial observations about it: Who are the characters? What’s going on? Where is this taking place? Revisit the part of your brain that houses all of that literary criticism stuff your English teacher went on and on about while you doodled with your new four-color ballpoint pen. As a quick refresher, remember to ask yourself some basic who- what-when-where observational questions:</p>\\n<p><i>WHO</i>&nbsp;is speaking? Who is this about? Who are the main characters?</p>\\n<p><i>WHAT</i>&nbsp;is the subject or event covered in the chapter? What do you learn about the people, event, or teaching?</p>\\n<p><i>WHEN</i>&nbsp;do/will the events occur or did/will something happen to someone?</p>\\n<p><i>WHERE</i>&nbsp;did or will this happen? Where was it said?</p>\\n<p><i>WHY</i>&nbsp;is something being said or mentioned? Why would/will this happen? Why at that time and/or to this person/people?</p>\\n<p><i>HOW</i>&nbsp;will it happen? How is it to be done? How is it illustrated?</p>\\n<p>I can never remember the “how,” because it rebelliously begins with an h instead of a w. Maybe it’s the same for you. If so, that’s okay. This is just a general template; it’s not meant to be a science or&nbsp;a straitjacket. Once you get a feel for some of these questions, try to keep an eye out for key words or phrases, repeated words, contrasts and comparisons, and terms of summary and conclusions (“so that,” “for this reason,” and so on).</p>\\n<p>INTERPRETATION</p>\\n<p>Often observation slides right into interpretation. But strictly speaking, observation refers to trying to understand what’s being said, while interpretation refers to understanding the overall meaning.</p>\\n<p>From what you’ve learned in your observation&nbsp;of the text, you are trying to discern a primary meaning of the passage—what the biblical author was seeking to communicate and what God was seeking to communicate through that biblical author. A particularly fruitful way to pull these things out&nbsp;of the passage is to ask questions such as “What sinful, broken, or fallen condition is being addressed or corrected by the passage?” “What is the deeper&nbsp;sin beneath the behavior?” and “What prompted&nbsp;the author to write this passage?” Keeping these questions in mind can help you uncover the primary meaning of the text.</p>\\n<p>APPLICATION</p>\\n<p>So, how does the passage apply to you and to others? And what are some actions you need to take in order to apply God’s Word directly to your life?</p>\\n<p>Let me suggest something here. I think our reflections&nbsp;tend to skip along the surface level of behavior or habits that need changing. This is okay, but often a more significant application resides at the level of our relating to God—our experiencing his grace or trusting in his character or promises. In light of this, I like to pray through certain questions that help me to apply what I’ve been reading to my walk with God. God, what are you trying to say to me through this passage?</p>\\n<p>In what ways am I looking to find life in things besides you, Lord?</p>\\n<p>In what ways am I seeking to earn your favor rather than live by grace?</p>\\n<p>What do my actions suggest that I’m wrongly believing about your character, our relationship, and what you’ve said in your Word?</p>\\n<p>In what ways do we need to be more intimate in our relationship?</p>\\n<p>In what way is Jesus the answer to my problem?</p>\\n<p>I particularly like thinking about the last one: In what way does Jesus provide an answer or solution to my sin problem? It’s hard to get into too much theological trouble with a question like this.</p>\\n\"";
//        article.mTitle = "Romances with wolves";
//        java.util.Date createdDate = new java.util.Date("Fri Jun 08 2018 18:55:00 GMT+0000");
//        article.mDateCreated = new Date(createdDate.getTime());
//
//        mArticleDao.insertArticle(article);
//
//        List<Article> articles = mArticleDao.getAllArticles().getValue();
//        assertThat(articles.get(0), equalTo(article) );
    }
}
