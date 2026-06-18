# MRT/BTS Group Data Cleaning Report

Generated: 2026-06-18

## Summary of input data

Source files:
- `C:\Users\Admin\Downloads\B8E089B8E084B8E0B2B8%A1.md`: Markdown report containing BTS Group line profiles, partial station snippets, fare-rule prose, and malformed JSON/SQL examples.
- `C:\Users\Admin\Downloads\gemini-code-1781773096961.md`: Markdown prompt/data file containing parseable Thai fare tables for MRT Pink Line and MRT Yellow Line.

Detected tables/data structures:
- Line profiles: 5 rows extracted from the BTS Group profile table.
- Fare station rows: 55 rows extracted from Pink/Yellow Markdown fare tables.
- Fare matrix values: 220 normalized fare rows from reference origins PK01/PK30/YL01/YL23 and ticket types adult/senior.
- Station coordinate details: 8 station rows enriched from the partial station snippets in the report.

## Assumptions applied

- No missing English station names were invented. Missing `station_name_en` values are null.
- No missing coordinates were invented. Missing `latitude`/`longitude` values are null.
- Fare tables are interpreted as one-way fare lookup rows from the listed reference origin columns.
- Currency is standardized to `THB` because all fare values are Baht in the source text.
- Date fields are ISO 8601-capable. Unknown effective dates from the Thai fare table are null.

## Validation report

Issue counts by severity:
- warning: 95

### Validation issues

- [warning] stations.station_name_en (PK02): English station name not present in source; left null.
- [warning] stations.latitude/longitude (PK02): Coordinates not present in source; left null.
- [warning] stations.station_name_en (PK03): English station name not present in source; left null.
- [warning] stations.latitude/longitude (PK03): Coordinates not present in source; left null.
- [warning] stations.station_name_en (PK04): English station name not present in source; left null.
- [warning] stations.latitude/longitude (PK04): Coordinates not present in source; left null.
- [warning] stations.station_name_en (PK05): English station name not present in source; left null.
- [warning] stations.latitude/longitude (PK05): Coordinates not present in source; left null.
- [warning] stations.station_name_en (PK06): English station name not present in source; left null.
- [warning] stations.latitude/longitude (PK06): Coordinates not present in source; left null.
- [warning] stations.station_name_en (PK07): English station name not present in source; left null.
- [warning] stations.latitude/longitude (PK07): Coordinates not present in source; left null.
- [warning] stations.station_name_en (PK08): English station name not present in source; left null.
- [warning] stations.latitude/longitude (PK08): Coordinates not present in source; left null.
- [warning] stations.station_name_en (PK09): English station name not present in source; left null.
- [warning] stations.latitude/longitude (PK09): Coordinates not present in source; left null.
- [warning] stations.station_name_en (PK10): English station name not present in source; left null.
- [warning] stations.latitude/longitude (PK10): Coordinates not present in source; left null.
- [warning] stations.station_name_en (PK11): English station name not present in source; left null.
- [warning] stations.latitude/longitude (PK11): Coordinates not present in source; left null.
- [warning] stations.station_name_en (PK12): English station name not present in source; left null.
- [warning] stations.latitude/longitude (PK12): Coordinates not present in source; left null.
- [warning] stations.station_name_en (PK13): English station name not present in source; left null.
- [warning] stations.latitude/longitude (PK13): Coordinates not present in source; left null.
- [warning] stations.station_name_en (PK14): English station name not present in source; left null.
- [warning] stations.latitude/longitude (PK14): Coordinates not present in source; left null.
- [warning] stations.station_name_en (PK15): English station name not present in source; left null.
- [warning] stations.latitude/longitude (PK15): Coordinates not present in source; left null.
- [warning] stations.station_name_en (PK17): English station name not present in source; left null.
- [warning] stations.latitude/longitude (PK17): Coordinates not present in source; left null.
- [warning] stations.station_name_en (PK18): English station name not present in source; left null.
- [warning] stations.latitude/longitude (PK18): Coordinates not present in source; left null.
- [warning] stations.station_name_en (PK19): English station name not present in source; left null.
- [warning] stations.latitude/longitude (PK19): Coordinates not present in source; left null.
- [warning] stations.station_name_en (PK20): English station name not present in source; left null.
- [warning] stations.latitude/longitude (PK20): Coordinates not present in source; left null.
- [warning] stations.station_name_en (PK21): English station name not present in source; left null.
- [warning] stations.latitude/longitude (PK21): Coordinates not present in source; left null.
- [warning] stations.station_name_en (PK22): English station name not present in source; left null.
- [warning] stations.latitude/longitude (PK22): Coordinates not present in source; left null.
- [warning] stations.station_name_en (PK23): English station name not present in source; left null.
- [warning] stations.latitude/longitude (PK23): Coordinates not present in source; left null.
- [warning] stations.station_name_en (PK24): English station name not present in source; left null.
- [warning] stations.latitude/longitude (PK24): Coordinates not present in source; left null.
- [warning] stations.station_name_en (PK25): English station name not present in source; left null.
- [warning] stations.latitude/longitude (PK25): Coordinates not present in source; left null.
- [warning] stations.station_name_en (PK26): English station name not present in source; left null.
- [warning] stations.latitude/longitude (PK26): Coordinates not present in source; left null.
- [warning] stations.station_name_en (PK27): English station name not present in source; left null.
- [warning] stations.latitude/longitude (PK27): Coordinates not present in source; left null.
- [warning] stations.station_name_en (PK28): English station name not present in source; left null.
- [warning] stations.latitude/longitude (PK28): Coordinates not present in source; left null.
- [warning] stations.station_name_en (PK29): English station name not present in source; left null.
- [warning] stations.latitude/longitude (PK29): Coordinates not present in source; left null.
- [warning] stations.station_name_en (YL02): English station name not present in source; left null.
- [warning] stations.latitude/longitude (YL02): Coordinates not present in source; left null.
- [warning] stations.station_name_en (YL03): English station name not present in source; left null.
- [warning] stations.latitude/longitude (YL03): Coordinates not present in source; left null.
- [warning] stations.station_name_en (YL04): English station name not present in source; left null.
- [warning] stations.latitude/longitude (YL04): Coordinates not present in source; left null.
- [warning] stations.station_name_en (YL05): English station name not present in source; left null.
- [warning] stations.latitude/longitude (YL05): Coordinates not present in source; left null.
- [warning] stations.station_name_en (YL06): English station name not present in source; left null.
- [warning] stations.latitude/longitude (YL06): Coordinates not present in source; left null.
- [warning] stations.station_name_en (YL07): English station name not present in source; left null.
- [warning] stations.latitude/longitude (YL07): Coordinates not present in source; left null.
- [warning] stations.station_name_en (YL08): English station name not present in source; left null.
- [warning] stations.latitude/longitude (YL08): Coordinates not present in source; left null.
- [warning] stations.station_name_en (YL09): English station name not present in source; left null.
- [warning] stations.latitude/longitude (YL09): Coordinates not present in source; left null.
- [warning] stations.station_name_en (YL10): English station name not present in source; left null.
- [warning] stations.latitude/longitude (YL10): Coordinates not present in source; left null.
- [warning] stations.station_name_en (YL11): English station name not present in source; left null.
- [warning] stations.latitude/longitude (YL11): Coordinates not present in source; left null.
- [warning] stations.station_name_en (YL12): English station name not present in source; left null.
- [warning] stations.latitude/longitude (YL12): Coordinates not present in source; left null.
- [warning] stations.station_name_en (YL13): English station name not present in source; left null.
- [warning] stations.latitude/longitude (YL13): Coordinates not present in source; left null.
- [warning] stations.station_name_en (YL14): English station name not present in source; left null.
- [warning] stations.latitude/longitude (YL14): Coordinates not present in source; left null.
- [warning] stations.station_name_en (YL15): English station name not present in source; left null.
- [warning] stations.latitude/longitude (YL15): Coordinates not present in source; left null.
- [warning] stations.station_name_en (YL16): English station name not present in source; left null.
- [warning] stations.latitude/longitude (YL16): Coordinates not present in source; left null.
- [warning] stations.station_name_en (YL18): English station name not present in source; left null.
- [warning] stations.latitude/longitude (YL18): Coordinates not present in source; left null.
- [warning] stations.station_name_en (YL19): English station name not present in source; left null.
- [warning] stations.latitude/longitude (YL19): Coordinates not present in source; left null.
- [warning] stations.station_name_en (YL20): English station name not present in source; left null.
- [warning] stations.latitude/longitude (YL20): Coordinates not present in source; left null.
- [warning] stations.station_name_en (YL21): English station name not present in source; left null.
- [warning] stations.latitude/longitude (YL21): Coordinates not present in source; left null.
- [warning] stations.station_name_en (YL22): English station name not present in source; left null.
- [warning] stations.latitude/longitude (YL22): Coordinates not present in source; left null.
- [warning] interchanges.station_id (PK01_PP11): Interchange references a station not present in cleaned MRT station table; preserved for review.


## Cleaned and normalized dataset

CSV files:
- `operators.csv`
- `lines.csv`
- `stations.csv`
- `routes.csv`
- `route_stops.csv`
- `ticket_types.csv`
- `payment_methods.csv`
- `fare_rules.csv`
- `fare_matrix.csv`
- `interchanges.csv`
- `validation_issues.csv`

JSON files:
- `mrt_dataset.normalized.json`: full normalized export.
- `mrt_dataset.mobile.json`: compact mobile/offline export.

SQL files:
- `schema.sqlite.sql`
- `schema.postgres.sql`
- `schema.mysql.sql`
- `inserts.sqlite.sql`
- `mrt_database.sqlite.sql`

## Records removed or merged

- Duplicate station rows by station code: none found in parsed fare rows.
- Invalid fare rows: none removed from the parseable Markdown tables.
- Malformed JSON/Room/SQL snippets in `B8E089B8E084B8E0B2B8%A1.md` were not imported as executable source data; reliable values were extracted only from parseable line-profile prose/table and station snippets.

## Fields that could not be validated

- Most English station names for PK/YL stations were not present in the fare source.
- Most coordinates/opening years/train times were not present in the fare source.
- Some interchanges from the malformed report reference stations outside the extracted MRT fare-table station set; these are preserved in `interchanges.csv` with `is_valid_reference=false` when applicable.

## Mobile optimization notes

- `mrt_dataset.mobile.json` uses compact nested arrays for offline loading.
- SQLite indexes are included for station line/sequence lookup, coordinate lookup, route stop lookup, and fare lookup.
- Fare search should query `fare_matrix` by `(line_id, origin_station_id, destination_station_id, ticket_type_id)`.

## Recommendations

1. Provide official station English names and coordinates for every PK/YL station to remove nulls.
2. Provide official effective dates for Pink/Yellow fare tables.
3. Provide full origin-destination fare matrices if fares are not symmetric or if all origins must be supported.
4. Provide machine-readable source data as CSV/JSON instead of PDF/Markdown to reduce extraction risk.
5. Confirm whether MT01/MT02 should be modeled as Pink Line branch stations or a separate route branch in production routing.
