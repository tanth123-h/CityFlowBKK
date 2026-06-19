CREATE TABLE IF NOT EXISTS operators (
  operator_id TEXT PRIMARY KEY,
  operator_name TEXT NOT NULL,
  operator_short_name TEXT
);
CREATE TABLE IF NOT EXISTS lines (
  line_id TEXT PRIMARY KEY,
  line_name TEXT NOT NULL,
  operator_id TEXT REFERENCES operators(operator_id),
  system TEXT NOT NULL,
  color_name TEXT,
  color_hex TEXT,
  route_length_km DOUBLE PRECISION,
  station_count_declared INTEGER,
  end_to_end_min INTEGER
);
CREATE TABLE IF NOT EXISTS stations (
  station_id TEXT PRIMARY KEY,
  line_id TEXT NOT NULL REFERENCES lines(line_id),
  station_code TEXT NOT NULL UNIQUE,
  station_name_en TEXT,
  station_name_th TEXT NOT NULL,
  sequence INTEGER,
  latitude DOUBLE PRECISION,
  longitude DOUBLE PRECISION,
  opening_year INTEGER,
  first_train TEXT,
  last_train TEXT,
  source_file TEXT
);
CREATE TABLE IF NOT EXISTS routes (
  route_id TEXT PRIMARY KEY,
  line_id TEXT NOT NULL REFERENCES lines(line_id),
  route_name TEXT NOT NULL,
  direction TEXT,
  source_file TEXT
);
CREATE TABLE IF NOT EXISTS route_stops (
  route_id TEXT NOT NULL REFERENCES routes(route_id),
  station_id TEXT NOT NULL REFERENCES stations(station_id),
  stop_sequence INTEGER NOT NULL,
  PRIMARY KEY (route_id, station_id)
);
CREATE TABLE IF NOT EXISTS ticket_types (
  ticket_type_id TEXT PRIMARY KEY,
  ticket_type_name TEXT NOT NULL,
  discount_percent DOUBLE PRECISION
);
CREATE TABLE IF NOT EXISTS payment_methods (
  payment_method_id TEXT PRIMARY KEY,
  payment_method_name TEXT NOT NULL,
  notes TEXT
);
CREATE TABLE IF NOT EXISTS fare_rules (
  fare_rule_id TEXT PRIMARY KEY,
  line_id TEXT NOT NULL REFERENCES lines(line_id),
  ticket_type_id TEXT NOT NULL REFERENCES ticket_types(ticket_type_id),
  currency TEXT NOT NULL DEFAULT 'THB',
  min_fare DOUBLE PRECISION,
  max_fare DOUBLE PRECISION,
  basis TEXT,
  effective_date TEXT
);
CREATE TABLE IF NOT EXISTS fare_matrix (
  fare_id TEXT PRIMARY KEY,
  line_id TEXT NOT NULL REFERENCES lines(line_id),
  origin_station_id TEXT NOT NULL REFERENCES stations(station_id),
  destination_station_id TEXT NOT NULL REFERENCES stations(station_id),
  ticket_type_id TEXT NOT NULL REFERENCES ticket_types(ticket_type_id),
  currency TEXT NOT NULL DEFAULT 'THB',
  fare_amount DOUBLE PRECISION NOT NULL CHECK (fare_amount >= 0),
  source_file TEXT
);
CREATE TABLE IF NOT EXISTS interchanges (
  interchange_id TEXT PRIMARY KEY,
  station_id_a TEXT NOT NULL,
  station_id_b TEXT NOT NULL,
  is_valid_reference INTEGER NOT NULL CHECK (is_valid_reference IN (0,1))
);
CREATE INDEX IF NOT EXISTS idx_stations_line_seq ON stations(line_id, sequence);
CREATE INDEX IF NOT EXISTS idx_stations_coords ON stations(latitude, longitude);
CREATE INDEX IF NOT EXISTS idx_fare_lookup ON fare_matrix(line_id, origin_station_id, destination_station_id, ticket_type_id);
CREATE INDEX IF NOT EXISTS idx_route_stops_station ON route_stops(station_id);
