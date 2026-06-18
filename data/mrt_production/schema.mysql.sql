CREATE TABLE IF NOT EXISTS operators (
  operator_id VARCHAR(255) PRIMARY KEY,
  operator_name VARCHAR(255) NOT NULL,
  operator_short_name VARCHAR(255)
);
CREATE TABLE IF NOT EXISTS lines (
  line_id VARCHAR(255) PRIMARY KEY,
  line_name VARCHAR(255) NOT NULL,
  operator_id VARCHAR(255) REFERENCES operators(operator_id),
  system VARCHAR(255) NOT NULL,
  color_name VARCHAR(255),
  color_hex VARCHAR(255),
  route_length_km REAL,
  station_count_declared INTEGER,
  end_to_end_min INTEGER
);
CREATE TABLE IF NOT EXISTS stations (
  station_id VARCHAR(255) PRIMARY KEY,
  line_id VARCHAR(255) NOT NULL REFERENCES lines(line_id),
  station_code VARCHAR(255) NOT NULL UNIQUE,
  station_name_en VARCHAR(255),
  station_name_th VARCHAR(255) NOT NULL,
  sequence INTEGER,
  latitude REAL,
  longitude REAL,
  opening_year INTEGER,
  first_train VARCHAR(255),
  last_train VARCHAR(255),
  source_file VARCHAR(255)
);
CREATE TABLE IF NOT EXISTS routes (
  route_id VARCHAR(255) PRIMARY KEY,
  line_id VARCHAR(255) NOT NULL REFERENCES lines(line_id),
  route_name VARCHAR(255) NOT NULL,
  direction VARCHAR(255),
  source_file VARCHAR(255)
);
CREATE TABLE IF NOT EXISTS route_stops (
  route_id VARCHAR(255) NOT NULL REFERENCES routes(route_id),
  station_id VARCHAR(255) NOT NULL REFERENCES stations(station_id),
  stop_sequence INTEGER NOT NULL,
  PRIMARY KEY (route_id, station_id)
);
CREATE TABLE IF NOT EXISTS ticket_types (
  ticket_type_id VARCHAR(255) PRIMARY KEY,
  ticket_type_name VARCHAR(255) NOT NULL,
  discount_percent REAL
);
CREATE TABLE IF NOT EXISTS payment_methods (
  payment_method_id VARCHAR(255) PRIMARY KEY,
  payment_method_name VARCHAR(255) NOT NULL,
  notes VARCHAR(255)
);
CREATE TABLE IF NOT EXISTS fare_rules (
  fare_rule_id VARCHAR(255) PRIMARY KEY,
  line_id VARCHAR(255) NOT NULL REFERENCES lines(line_id),
  ticket_type_id VARCHAR(255) NOT NULL REFERENCES ticket_types(ticket_type_id),
  currency VARCHAR(255) NOT NULL DEFAULT 'THB',
  min_fare REAL,
  max_fare REAL,
  basis VARCHAR(255),
  effective_date VARCHAR(255)
);
CREATE TABLE IF NOT EXISTS fare_matrix (
  fare_id VARCHAR(255) PRIMARY KEY,
  line_id VARCHAR(255) NOT NULL REFERENCES lines(line_id),
  origin_station_id VARCHAR(255) NOT NULL REFERENCES stations(station_id),
  destination_station_id VARCHAR(255) NOT NULL REFERENCES stations(station_id),
  ticket_type_id VARCHAR(255) NOT NULL REFERENCES ticket_types(ticket_type_id),
  currency VARCHAR(255) NOT NULL DEFAULT 'THB',
  fare_amount REAL NOT NULL CHECK (fare_amount >= 0),
  source_file VARCHAR(255)
);
CREATE TABLE IF NOT EXISTS interchanges (
  interchange_id VARCHAR(255) PRIMARY KEY,
  station_id_a VARCHAR(255) NOT NULL,
  station_id_b VARCHAR(255) NOT NULL,
  is_valid_reference INTEGER NOT NULL CHECK (is_valid_reference IN (0,1))
);
CREATE INDEX IF NOT EXISTS idx_stations_line_seq ON stations(line_id, sequence);
CREATE INDEX IF NOT EXISTS idx_stations_coords ON stations(latitude, longitude);
CREATE INDEX IF NOT EXISTS idx_fare_lookup ON fare_matrix(line_id, origin_station_id, destination_station_id, ticket_type_id);
CREATE INDEX IF NOT EXISTS idx_route_stops_station ON route_stops(station_id);
