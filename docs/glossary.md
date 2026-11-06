# Glossary

| French | English (code) | Displayed (FR) | Displayed (EN) | Definition | Forbidden synonyms |
| --- | --- | --- | --- | --- | --- |
| tendance 7 jours | 7-day trend | Tendance 7 jours |  | Chart of the daily forecasts of the next 7 days. |  |
| recherche de ville | city search | Rechercher une ville… |  | Search of a city by name through Open-Meteo geocoding. |  |
| position actuelle | current location | Ma position |  | Location of the device, from GPS; Paris when the permission is denied. | géolocalisation |
| prévision journalière | daily forecast | 15 jours |  | Forecast of one day: minimum and maximum temperatures, wind, precipitation, weather condition (DayForecast). | prévision quotidienne |
| détails horaires | day hourly details | Détails horaires |  | Hourly forecasts of one chosen day, loaded on demand. |  |
| résumé du jour | day summary | Aujourd’hui |  | Today's minimum, maximum and current temperatures with its weather condition (DayPeriod in the code). |  |
| lieu par défaut | default location | Paris (par défaut) |  | Favorite opened at startup (isDefault); Paris when none is set. |  |
| bascule | fallback |  |  | Switch to the next weather provider when the current one fails. | repli |
| favori | favorite | Favoris |  | Location saved by the user for quick access (SavedLocation in the code). | emplacement sauvegardé, lieu enregistré |
| prévision horaire | hourly forecast | Prochaines heures |  | Forecast of one hour: temperature, precipitation, wind, weather condition (HourRow). |  |
| libellé | label |  |  | Displayed text of a weather condition. | description |
| lieu | location | localisation |  | Place whose weather is shown: a favorite, a search result or the current location. | emplacement, localisation, endroit |
| mode hors connexion | offline mode | Mode hors connexion - Données locales |  | Display of the last fetched weather data when the network is unavailable. | cache |
| aperçu | overview | Aujourd’hui et demain |  | Top section: day summary of today and the periods of today and tomorrow. | synthèse |
| période | period | Matin, Midi, Après-midi, Soirée |  | Part of a day with its forecast (PeriodSlot, NamedPeriod; types in DayPeriodType). | créneau, tranche |
| cumul de précipitations | precipitation amount |  |  | Expected precipitation, in millimetres (precipAmount). | quantité de pluie |
| probabilité de précipitations | precipitation probability | Pluie % |  | Chance of precipitation, in percent (precipPct). | risque de pluie |
| lieux récents | recent locations | Récents |  | Locations opened lately, listed under the favorites. | historique |
| fond météo | weather background |  |  | Generated background image of a weather condition (bg_*). | fond d'écran, wallpaper |
| condition météo | weather condition | Dégagé, Couvert… |  | Kind of weather derived from a WMO code, with its label, icon and background image. | temps, état du ciel |
| météo | weather data |  |  | Everything fetched for one location: overview, hourly and daily forecasts (WeatherData). |  |
| fournisseur météo | weather provider |  |  | Weather data source behind WeatherProvider: Open-Meteo first, MET Norway as fallback. | source, API météo |
| vitesse du vent | wind speed | Vent |  | Wind speed in km/h; daily forecasts carry a minimum and a maximum. |  |
| code WMO | WMO code |  |  | Numeric weather code of the World Meteorological Organization, mapped to a weather condition. |  |
