package com.mili.xiaominglui.app.vello.data.factory;

import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Log;
import android.util.Xml;

import com.mili.xiaominglui.app.vello.data.model.Definition;
import com.mili.xiaominglui.app.vello.data.model.Definitions;
import com.mili.xiaominglui.app.vello.data.model.IcibaWord;
import com.mili.xiaominglui.app.vello.data.model.Phonetics;
import com.mili.xiaominglui.app.vello.data.model.Phoneticss;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


@Deprecated
public class IcibaWordXmlParser {
    private static final String TAG = IcibaWordXmlParser.class.getSimpleName();
    private static IcibaWord word;
    private static Phoneticss phoneticss;
    private static Phonetics phonetics;
    private static Definitions definitions;
    private static Definition definition;
    
    public static IcibaWord parse(String xml) {
	RootElement dict = new RootElement("dict");
	Element dictPs = dict.getChild("ps");
	Element dictPron = dict.getChild("pron");
	Element dictPos = dict.getChild("pos");
	Element dictAcceptation = dict.getChild("acceptation");
	
	dict.setStartElementListener(new StartElementListener() {
	    
	    @Override
	    public void start(Attributes attributes) {
		word = new IcibaWord();
		phoneticss = new Phoneticss();
		definitions = new Definitions();
	    }
	});
	dictPs.setStartElementListener(new StartElementListener() {
	    
	    @Override
	    public void start(Attributes arg0) {
		phonetics = new Phonetics();
	    }
	});
	
	dictPs.setEndTextElementListener(new EndTextElementListener() {
	    
	    @Override
	    public void end(String body) {
		phonetics.symbol = body;
	    }
	});
	
	dictPron.setEndTextElementListener(new EndTextElementListener() {
	    
	    @Override
	    public void end(String body) {
		phonetics.sound = body;
		phoneticss.add(phonetics);
	    }
	});
	
	dictPos.setStartElementListener(new StartElementListener() {
	    
	    @Override
	    public void start(Attributes attributes) {
		definition = new Definition();
	    }
	});
	
	dictPos.setEndTextElementListener(new EndTextElementListener() {
	    
	    @Override
	    public void end(String body) {
		definition.pos = body;
	    }
	});
	
	dictAcceptation.setEndTextElementListener(new EndTextElementListener() {
	    
	    @Override
	    public void end(String body) {
		definition.definiens = body;
		definitions.add(definition);
	    }
	});
	
	dict.setEndElementListener(new EndElementListener() {
	    
	    @Override
	    public void end() {
		word.phonetics = phoneticss;
		word.definition = definitions;
	    }
	});
	
	try {
	    Xml.parse(xml, dict.getContentHandler());
	    return word;
	} catch (SAXException e) {
	    Log.e(TAG, "SAXException", e);
	}
	return null;
    }
}
