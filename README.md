# CityFlowBKK 🚇

**Bangkok Smart Transit & Discovery App**

CityFlowBKK is an open-source Android application that helps residents, workers, and tourists navigate Bangkok's public transport system with ease. Plan BTS/MRT routes, discover attractions, and explore the city — all in one app.

---

## Screenshots

![Uploading image.png…]()


---

## Features

| Feature | Description |
|---|---|
| 🗺 **Route Planner** | Step-by-step BTS/MRT transit routing with Google Directions API |
| 💰 **Fare Calculator** | Real-time BTS and MRT fare calculation per journey |
| 🔔 **Arrival Alerts** | Proximity-based push notifications before reaching your station |
| 🏙 **Discover Bangkok** | Swipe-to-discover Bangkok attractions (Tinder-style UI) |
| ❤️ **Saved Places** | Save favourite spots and plan routes directly from them |
| ⭐ **Recommended Destinations** | Category-based recommendation engine with one-tap route planning |
| 🚉 **BTS Network Map** | Interactive BTS station map with calibrated overlay |
| 📍 **Place Details** | Real-time opening hours, ratings, photos, and embedded map preview |

---

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material Design 3
- **Architecture:** MVVM + Repository Pattern + StateFlow
- **Navigation:** Navigation Compose
- **Maps:** Google Maps SDK for Android, Maps Compose
- **APIs:** Google Directions API, Google Places API, Google Geocoding API
- **Image Loading:** Coil 3
- **Networking:** Retrofit + OkHttp
- **Build:** Gradle (Kotlin DSL), Android Studio

---

## Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- JDK 17+ (Android Studio bundled JBR recommended)
- Android device or emulator running API 24+
- Google Maps API key (Places API + Directions API + Maps SDK enabled)

### Setup

1. Clone the repository
   ```bash
   git clone https://github.com/tanth123-h/CityFlowBKK.git
   cd CityFlowBKK
   ```

2. Add your API keys to `gradle.properties` (never commit this file with real keys):
   ```properties
   MAPS_API_KEY=your_google_maps_api_key
   GOOGLE_MAPS_API_KEY=your_google_maps_api_key
   GOOGLE_PLACES_API_KEY=your_google_places_api_key
   ```

3. Open the project in Android Studio and run on a device or emulator.

> ⚠️ `local.properties` and `gradle.properties` containing API keys are in `.gitignore` and will never be committed.

---

## Project Structure

```
app/src/main/java/com/example/cityflowbkk/
├── features/
│   ├── home/          # Home screen, recommendations, quick actions
│   ├── route/         # Route planning, navigation, fare calculation
│   ├── tour/          # Discover Bangkok, saved places, place detail
│   ├── btsmap/        # BTS network map
│   ├── map/           # Google Maps integration, directions
│   └── place/         # Place detail bottom sheet
├── navigation/        # NavGraph, Screen routes, bottom nav
├── data/              # Places repository, Google Places service
└── ui/                # Theme, icons, shared components
```

---

## Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/your-feature`)
3. Commit your changes (`git commit -m 'Add your feature'`)
4. Push to the branch (`git push origin feature/your-feature`)
5. Open a Pull Request

---

## License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

---

## Team

**พยัคฆ์เมฆา x รัตติกาล** — HacKaTech: ศึกชิงเก้าอี้ผู้ว่ากทม69

| Name | Role |
|---|---|
| Tankhun Srijankaew | Project Lead / Developer / Pitcher |
| Patcharapon Ladlao | Developer / Designer |
| Nutthanapon Sannok | Developer (Route & Fare) |
| Yingkun Sukjaras | Developer / Designer (BTS/MRT Screens) |
| Rattapoom Mora | Researcher / Developer |
| Ratchakrit Nittayacharoendet | Researcher / Developer |
| Somprasong Thunnok | Developer / Pitcher |

---

## Acknowledgements

- [Google Maps Platform](https://developers.google.com/maps)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Maps Compose](https://github.com/googlemaps/android-maps-compose)
- [Coil](https://coil-kt.github.io/coil/)
- Bangkok Open Data & TDRI Transit Research
