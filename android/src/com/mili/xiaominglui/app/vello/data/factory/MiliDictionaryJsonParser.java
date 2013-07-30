package com.mili.xiaominglui.app.vello.data.factory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mili.xiaominglui.app.vello.config.JSONTag;
import com.mili.xiaominglui.app.vello.data.model.Definition;
import com.mili.xiaominglui.app.vello.data.model.Definitions;
import com.mili.xiaominglui.app.vello.data.model.IcibaWord;
import com.mili.xiaominglui.app.vello.data.model.Phonetics;
import com.mili.xiaominglui.app.vello.data.model.Phoneticss;

public class MiliDictionaryJsonParser {
	private static String TAG = MiliDictionaryJsonParser.class.getSimpleName();
	private static IcibaWord word;
	private static Phoneticss phoneticss;
	private static Definitions definitions;

	public static IcibaWord parse(String desc) {
		word = new IcibaWord();
		phoneticss = new Phoneticss();
		definitions = new Definitions();
		try {
			JSONObject jsonMiliWord = new JSONObject(desc);
			word.keyword = jsonMiliWord
					.getString(JSONTag.MILI_DICTIONARY_SPELL);
			JSONArray jsonPhoneticss = jsonMiliWord
					.getJSONArray(JSONTag.MILI_DICTIONARY_PRON);
			int l = jsonPhoneticss.length();
			for (int i = 0; i < l; i++) {
				Phonetics p = new Phonetics();
				JSONObject jsonPhonetics = jsonPhoneticss.getJSONObject(i);
				p.sound = jsonPhonetics
						.getString(JSONTag.MILI_DICTIONARY_PRON_LINK);
				p.style = jsonPhonetics
						.getString(JSONTag.MILI_DICTIONARY_PRON_TYPE);
				p.symbol = jsonPhonetics
						.getString(JSONTag.MILI_DICTIONARY_PRON_PS);
				phoneticss.add(p);
			}
			JSONArray jsonDefinitions = jsonMiliWord
					.getJSONArray(JSONTag.MILI_DICTIONARY_ACCETTATION);
			int m = jsonDefinitions.length();
			for (int i = 0; i < m; i++) {
				Definition d = new Definition();
				JSONObject jsonDefinition = jsonDefinitions.getJSONObject(i);
				d.pos = jsonDefinition
						.getString(JSONTag.MILI_DICTIONARY_ACCETTATION_POS);
				d.definiens = jsonDefinition
						.getString(JSONTag.MILI_DICTIONARY_ACCETTATION_ACCEP);
				definitions.add(d);
			}
			word.phonetics = phoneticss;
			word.definition = definitions;
			return word;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
}
