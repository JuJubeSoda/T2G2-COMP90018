# MyPlants Package Documentation Guide

## Overview
This guide provides comprehensive documentation templates and guidelines for all files in the `ui/myplants` package. Use these templates to maintain consistent, professional-grade documentation across the codebase.

---

## ✅ Completed Files (5/16 Java Files)

1. ✅ **Plant.java** - Core domain model (FULLY DOCUMENTED)
2. ✅ **UploadFragment.java** - Plant upload handler (FULLY DOCUMENTED)
3. ✅ **CaptureFragment.java** - Camera capture (PARTIALLY DOCUMENTED - needs enhancement)

---

## 📋 Documentation Standards Established

### Class-Level Documentation Template
```java
/**
 * ClassName - Brief one-line purpose.
 * 
 * Purpose/Responsibilities:
 * - Key responsibility 1
 * - Key responsibility 2
 * 
 * User Flow: (for UI components)
 * 1. Step 1
 * 2. Step 2
 * 3. Step 3
 * 
 * Key Features:
 * - Feature with explanation
 * 
 * Data Flow: (if applicable)
 * Input → Processing → Output
 * 
 * Navigation: (for fragments)
 * - From: SourceFragment
 * - To: DestinationFragment
 * - Arguments: What data is passed
 * 
 * API Integration: (if applicable)
 * - Endpoints used
 * - Request/Response formats
 */
```

---

## 📚 Remaining Files Documentation Plan

### 1. AddPlantFragment.java
**Purpose**: Search and select plant from wiki before capture
**Key Comments Needed**:
```java
/**
 * AddPlantFragment - Plant name search and selection interface.
 * 
 * Purpose:
 * - Search plant wiki by name
 * - Display filtered results
 * - Pass selected plant to CaptureFragment
 * 
 * User Flow:
 * 1. User types plant name in search field
 * 2. Fragment filters wiki plants in real-time
 * 3. User selects plant from results
 * 4. Navigates to CaptureFragment with plant name pre-filled
 * 
 * Key Features:
 * - Real-time search filtering
 * - Shows all plants by default
 * - Filters as user types
 * - Passes scientificName to next screen
 * 
 * API Integration:
 * - GET /api/wiki/all - Fetches all wiki plants for search
 */
```

**Method Comments**:
- `fetchAllPlantNamesFromServer()` - API call to get wiki plants
- `filterPlantNames()` - Real-time search filtering logic
- `setupNextButton()` - Navigation with selected plant

---

### 2. MyGardenFragment.java
**Purpose**: Display user's plant collection in grid/list view
**Key Comments Needed**:
```java
/**
 * MyGardenFragment - User's personal plant collection display.
 * 
 * Purpose:
 * - Display all plants in user's garden
 * - Support grid and list view toggle
 * - Navigate to plant details
 * - Refresh data from server
 * 
 * User Flow:
 * 1. Fragment loads user's plants from API
 * 2. Displays in grid view (default)
 * 3. User can toggle to list view
 * 4. User clicks plant to see details
 * 
 * Key Features:
 * - Grid/List view toggle
 * - Pull-to-refresh
 * - Loading indicators
 * - Empty state handling
 * - Navigation to PlantDetailFragment
 * 
 * API Integration:
 * - GET /api/plants/user - Fetches user's plant collection
 * 
 * View Management:
 * - masterPlantList: Complete dataset
 * - Prevents unnecessary re-fetching on back navigation
 * - Handles view switching without data loss
 */
```

**Method Comments**:
- `fetchPlantsFromServer()` - API call with error handling
- `switchToGridView()` / `switchToListView()` - View toggle logic
- `updateToggleButtonsVisualState()` - UI state management

---

### 3. PlantWikiFragment.java
**Purpose**: Searchable encyclopedia of plants
**Key Comments Needed**:
```java
/**
 * PlantWikiFragment - Plant encyclopedia browser and search.
 * 
 * Purpose:
 * - Display all wiki plants in searchable grid
 * - Filter plants by name
 * - Navigate to detailed wiki pages
 * 
 * User Flow:
 * 1. Fragment loads all wiki plants from API
 * 2. Displays in grid with search bar
 * 3. User searches to filter results
 * 4. User clicks plant to see detailed wiki page
 * 
 * Key Features:
 * - Searchable plant grid
 * - Real-time filtering
 * - Loading indicators
 * - Navigation to PlantWikiMainTab with plant data
 * 
 * API Integration:
 * - GET /api/wiki/all - Returns List<PlantWikiDto>
 * - Converts PlantWikiDto to Plant via toPlant()
 * 
 * Navigation:
 * - To: PlantWikiMainTab (R.id.plantwiki_maintab)
 * - Passes: Plant object as Parcelable
 */
```

---

### 4. PlantDetailFragment.java
**Purpose**: Display comprehensive plant information
**Key Comments Needed**:
```java
/**
 * PlantDetailFragment - Detailed plant information display.
 * 
 * Purpose:
 * - Show complete plant information
 * - Display image, name, description
 * - Show care requirements
 * - Display features and care guide (for wiki plants)
 * 
 * User Flow:
 * 1. Receives Plant object from MyGarden or other source
 * 2. Populates all UI fields with plant data
 * 3. Handles missing data with fallbacks
 * 4. Displays wiki-specific fields if available
 * 
 * Key Features:
 * - Comprehensive plant information display
 * - Handles both user plants and wiki plants
 * - Null-safe field population
 * - Date parsing with multiple format support
 * - Image loading with Glide
 * - Care requirements grid
 * - Features and care guide sections
 * 
 * Data Handling:
 * - User Plants: Shows timestamps, location, user info
 * - Wiki Plants: Shows features, care guide, mature height
 * - Fallback: "Pending information" for missing fields
 */
```

---

### 5. PlantWikiMainTabFragment.java
**Purpose**: Container for wiki detail tabs
**Key Comments Needed**:
```java
/**
 * PlantWikiMainTabFragment - Tabbed interface for wiki plant details.
 * 
 * Purpose:
 * - Container for three wiki tabs (Overview, Features, Care Guide)
 * - Manages ViewPager2 and TabLayout
 * - Passes Plant object to child tabs
 * 
 * Tabs:
 * 1. Overview - Description, care requirements, sensor readings
 * 2. Features - Physical characteristics, mature height
 * 3. Care Guide - Complete care instructions
 * 
 * Navigation:
 * - From: PlantWikiFragment
 * - Receives: Plant object as Parcelable
 * - Distributes: Plant to all three tab fragments
 */
```

---

### 6. PlantWikiOverview.java
**Purpose**: Overview tab with sensor readings
**Key Comments Needed**:
```java
/**
 * PlantWikiOverview - Overview tab showing plant basics and live sensor data.
 * 
 * Purpose:
 * - Display plant description
 * - Show care requirements (water, light, temperature, humidity)
 * - Provide live sensor readings
 * - Convert raw sensor values to human-readable descriptions
 * 
 * Key Features:
 * - Description from wiki
 * - Care requirement cards
 * - Live sensor integration (light, temperature, humidity)
 * - Human-readable sensor descriptions
 * - Sensor reading legend
 * 
 * Sensor Integration:
 * - TYPE_LIGHT: Converts lux to "Very Bright", "Bright", etc.
 * - TYPE_AMBIENT_TEMPERATURE: Celsius readings
 * - TYPE_RELATIVE_HUMIDITY: Percentage readings
 * 
 * Sensor Descriptions:
 * - Light: Very Bright (>10000 lux), Bright (5000-10000), etc.
 * - Temperature: Actual °C value
 * - Humidity: Actual % value
 */
```

---

### 7. PlantWikiFeatures.java
**Purpose**: Features tab
**Key Comments Needed**:
```java
/**
 * PlantWikiFeatures - Features tab displaying plant characteristics.
 * 
 * Purpose:
 * - Display physical and botanical features
 * - Show mature height
 * - Present structured feature information
 * 
 * Data Sources:
 * - features: From PlantWikiDto.features
 * - matureHeight: From PlantWikiDto.growthHeight
 * 
 * Fallback:
 * - Shows "Pending information" if data not available
 */
```

---

### 8. PlantWikiCareGuide.java
**Purpose**: Care guide tab
**Key Comments Needed**:
```java
/**
 * PlantWikiCareGuide - Care guide tab with complete care instructions.
 * 
 * Purpose:
 * - Display comprehensive care guide
 * - Show watering instructions
 * - Show lighting requirements
 * - Present structured care information
 * 
 * Data Sources:
 * - careGuide: From PlantWikiDto.careGuide
 * - waterRequirement: Fallback if careGuide unavailable
 * - lightRequirement: Fallback if careGuide unavailable
 * 
 * Fallback Strategy:
 * 1. Try careGuide field first
 * 2. Fall back to waterRequirement/lightRequirement
 * 3. Show "Pending information" if all null
 */
```

---

### 9. TabsPagerAdapter.java
**Purpose**: ViewPager2 adapter for wiki tabs
**Key Comments Needed**:
```java
/**
 * TabsPagerAdapter - ViewPager2 adapter for PlantWiki detail tabs.
 * 
 * Purpose:
 * - Manages three wiki tab fragments
 * - Passes Plant object to each tab
 * - Handles fragment creation and lifecycle
 * 
 * Tabs (in order):
 * 0. PlantWikiOverview
 * 1. PlantWikiFeatures
 * 2. PlantWikiCareGuide
 * 
 * Data Passing:
 * - Receives Plant object in constructor
 * - Uses newInstance() factory methods
 * - Passes Plant as Parcelable to each tab
 */
```

---

### 10. PlantCardAdapter.java
**Purpose**: RecyclerView adapter for plant grid/list
**Key Comments Needed**:
```java
/**
 * PlantCardAdapter - RecyclerView adapter for displaying plants in grid or list.
 * 
 * Purpose:
 * - Display plants in grid or list layout
 * - Handle click events for navigation
 * - Load images with Glide
 * - Format dates and display plant info
 * 
 * View Types:
 * - Grid: item_plant_card.xml
 * - List: item_plant_card.xml (same layout, different LayoutManager)
 * 
 * Key Features:
 * - Image loading with Base64 support
 * - Date parsing with multiple formats
 * - Click listener for navigation
 * - Null-safe data binding
 * 
 * Data Handling:
 * - Accepts List<Plant>
 * - Supports dynamic data updates
 * - Handles missing images gracefully
 */
```

---

### 11. SearchResultAdapter.java
**Purpose**: Adapter for search results in AddPlantFragment
**Key Comments Needed**:
```java
/**
 * SearchResultAdapter - RecyclerView adapter for plant search results.
 * 
 * Purpose:
 * - Display filtered plant names
 * - Handle selection clicks
 * - Support real-time filtering
 * 
 * Used By: AddPlantFragment
 * 
 * Key Features:
 * - Simple text-based list
 * - Click listener for plant selection
 * - Dynamic filtering support
 */
```

---

### 12. UploadCompleteFragment.java
**Purpose**: Success screen after upload
**Key Comments Needed**:
```java
/**
 * UploadCompleteFragment - Upload success confirmation screen.
 * 
 * Purpose:
 * - Show upload success message
 * - Preview uploaded plant data
 * - Provide navigation options
 * 
 * User Flow:
 * 1. Receives plant data from UploadFragment
 * 2. Displays preview with image and details
 * 3. Offers navigation to Plant Wiki or My Garden
 * 
 * Navigation Options:
 * - "Gather More Information" → Plant Wiki
 * - Back navigation → My Garden
 * 
 * Data Received:
 * - Image URI
 * - Scientific name
 * - Description
 * - Location (formatted string)
 */
```

---

### 13. MainTabsFragment.java & PlantMapFragment.java
**Purpose**: Additional fragments (document if actively used)

---

## 🎨 XML Layout Documentation Template

```xml
<!--
    Layout: layout_name.xml
    Purpose: Brief description
    
    Used By: FragmentName.java
    
    Layout Structure:
    - Root: ConstraintLayout/LinearLayout/etc.
    - Key Sections:
      * Section 1: Purpose
      * Section 2: Purpose
    
    Key Components:
    - ComponentID: Purpose and behavior
    - ComponentID: Purpose and behavior
    
    Design Notes:
    - Material Design components used
    - Responsive behavior
    - Accessibility features
    
    Interactive Elements:
    - Button/Click targets
    - Input fields
    - Navigation triggers
-->
```

---

## 📝 Quick Reference: Comment Priorities

### High Priority (Document First):
1. ✅ Plant.java - DONE
2. ✅ UploadFragment.java - DONE
3. MyGardenFragment.java
4. PlantWikiFragment.java
5. PlantDetailFragment.java

### Medium Priority:
6. AddPlantFragment.java
7. PlantWikiMainTabFragment.java
8. PlantWikiOverview.java
9. PlantCardAdapter.java

### Lower Priority:
10. PlantWikiFeatures.java
11. PlantWikiCareGuide.java
12. TabsPagerAdapter.java
13. SearchResultAdapter.java
14. UploadCompleteFragment.java

---

## ✨ Documentation Quality Checklist

For each file, ensure:
- [ ] Class-level JavaDoc with purpose
- [ ] User flow documented (for UI components)
- [ ] Key methods have JavaDoc
- [ ] Complex logic has inline comments
- [ ] API endpoints documented
- [ ] Navigation paths explained
- [ ] Data flow clarified
- [ ] Error handling noted
- [ ] Null safety documented
- [ ] Field purposes explained

---

## 🎯 Consistency Guidelines

1. **Use consistent terminology**:
   - "Plant" not "plant object"
   - "Fragment" not "screen"
   - "API endpoint" not "backend call"

2. **Follow established patterns**:
   - See Plant.java for field documentation style
   - See UploadFragment.java for method documentation style
   - See HomeFragment.java for API call documentation

3. **Be concise but complete**:
   - One-line summaries for simple methods
   - Multi-line explanations for complex logic
   - Always explain "why" not just "what"

4. **Document edge cases**:
   - Null handling
   - Empty states
   - Error scenarios
   - Configuration changes

---

## 📊 Progress Tracking

**Java Files**: 3/16 completed (19%)
**XML Files**: 0/13 completed (0%)
**Total**: 3/29 completed (10%)

**Next Steps**:
1. Complete MyGardenFragment.java
2. Complete PlantWikiFragment.java
3. Complete PlantDetailFragment.java
4. Continue with remaining fragments
5. Document XML layouts

---

This guide ensures all documentation follows the same high-quality standards established in the completed files.

