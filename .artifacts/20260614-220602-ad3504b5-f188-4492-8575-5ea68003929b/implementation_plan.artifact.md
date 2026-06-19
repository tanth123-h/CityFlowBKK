# Redesign Home Screen UI/UX

Redesign the Home screen to feel minimal, premium, and spacious, aligned with modern transit apps like Google Maps and Apple Maps.

## Proposed Changes

### [Home Feature](file:///C:/Users/tankh/StudioProjects/CityFlowBKK/app/src/main/java/com/example/cityflowbkk/features/home/HomeScreen.kt)

#### [HomeScreen.kt](file:///C:/Users/tankh/StudioProjects/CityFlowBKK/app/src/main/java/com/example/cityflowbkk/features/home/HomeScreen.kt)

- **Remove Search Section**: Delete `WelcomeSection` and all search-related state/logic.
- **Remove Recent Searches**: Delete `RecentSearchesSection`, `RecentSearchUiModel`, and `sampleRecentSearches`.
- **Remove Tutorial**: Update `sampleQuickActions` to remove "Tutorials".
- **Redesign Quick Actions**:
    - Use larger rounded corners (24dp).
    - Improve grid layout for 3 columns.
    - Add modern Material 3 styling with subtle elevations and press effects.
- **Improve Hero Banner**:
    - Increase height.
    - Use premium gradients and better typography.
    - Add decorative elements.
- **Redesign Popular Places**:
    - Use larger cards with better aspect ratios.
    - Improve shadow and corner radius (24dp).
    - Enhance typography hierarchy.
- **Overall Layout**:
    - Increase spacing between sections (32dp).
    - Use consistent 24dp horizontal padding.
    - Add subtle transit-themed decorations (floating shapes, soft gradients).

---

## Verification Plan

### Automated Tests
- Run `:app:compileDebugKotlin` to ensure no unresolved references or unused variable warnings.

### Manual Verification
- Use `render_compose_preview` on `HomeScreenPreview` to visually verify the new design.
- Verify that "Plan Route" and place clicking still trigger their respective actions (navigation/bottom sheet).
