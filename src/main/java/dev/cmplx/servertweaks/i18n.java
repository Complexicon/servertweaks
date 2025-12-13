package dev.cmplx.servertweaks;

public enum i18n {

	/*
		Translations
	*/

	PLAYTIME_LIMIT_REACHED_MESSAGE("&4You have reached your weekly playtime limit."),
	JOIN_MESSAGE("&a{player} joined."),
	LEAVE_MESSAGE("&c{player} left."),
	DEATH_MESSAGE("&4{deathMessage}"),
	AFK_MESSAGE("&7{player} is now AFK"),
	AFK_RETURN_MESSAGE("&7{player} is no longer AFK"),
	PLAYTIME_LEADERBOARD_HEADING("&6Playtime - Top 5");

	/*
	
		INTERNALS
	
	*/

	private String translation;

	i18n(String translation) { this.translation = translation; }

	public static class TranslationMapping {
		public final String key;
		public final String value;

		public TranslationMapping(String key, String value) {
			this.key = key;
			this.value = value;
		}
	}

	public String fmt(TranslationMapping... args) {
		String workString = translation;
		
		for (var tm : args) {
			workString = workString.replace(String.format("{%s}", tm.key), tm.value);
		}

		return Util.fixColor(workString);
	}

	public void updateTranslation(String newTranslation) {
		this.translation = newTranslation;
	}

	public String get() {
		return translation;
	}

	public static TranslationMapping param(String key, String value) {
		return new TranslationMapping(key, value);
	}

}