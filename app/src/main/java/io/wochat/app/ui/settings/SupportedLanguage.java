package io.wochat.app.ui.settings;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class SupportedLanguage {

	@SerializedName("name")
	private String languageNameFromISO;

	@SerializedName("language")
	private String languageCode;

	@SerializedName("country_code")
	private String countryCode;

	public String getLanguageNameFromISO() {
		return languageNameFromISO;
	}

	public void setLanguageNameFromISO(String languageNameFromISO) {
		this.languageNameFromISO = languageNameFromISO;
	}

	public String getLanguageCode() {
		return languageCode;
	}

	public void setLanguageCode(String languageCode) {
		this.languageCode = languageCode;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public static SupportedLanguage fromJson (String json) {
		Gson gson = new Gson();
		return gson.fromJson(json, SupportedLanguage.class);
	}

}
