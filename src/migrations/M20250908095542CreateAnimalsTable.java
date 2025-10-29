package migrations;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class M20250908095542CreateAnimalsTable implements Migration {

    @Override
    public String name() {
        return "20250908095542_create_animals_table";
    }

    @Override
    public void up(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("""
                CREATE EXTENSION IF NOT EXISTS "pgcrypto";
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS public.animals (
                    uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                    name VARCHAR(255) NOT NULL,
                    sex VARCHAR(10) NOT NULL CHECK (sex IN ('male','female')),
                    species VARCHAR(10) NOT NULL CHECK (species IN ('dog','cat')),
                    breed VARCHAR(100) NOT NULL CHECK (breed IN (
                        'mixed-breed',
                        'shih-tzu',
                        'yorkshire-terrier',
                        'german-spitz',
                        'french-bulldog',
                        'poodle',
                        'lhasa-apso',
                        'golden-retriever',
                        'rottweiler',
                        'labrador-retriever',
                        'pug',
                        'german-shepherd',
                        'border-collie',
                        'long-haired-chihuahua',
                        'belgian-malinois',
                        'maltese'
                    )),
                    size VARCHAR(10) NOT NULL CHECK (size IN ('small','medium','large')),
                    color VARCHAR(20) NOT NULL CHECK (color IN ('black','white','gray','brown','golden','cream','tan','speckled')),
                    birthdate DATE,
                    microchip VARCHAR(20),
                    rga VARCHAR(20),
                    castrated BOOLEAN DEFAULT false,
                    fiv VARCHAR(10) CHECK (fiv IN ('yes','no','not-tested')) ,
                    felv VARCHAR(10) CHECK (felv IN ('yes','no','not-tested')),
                    status VARCHAR(20) CHECK (status IN ('quarantine','sheltered','adopted','lost')),
                    notes TEXT,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                );
            """);
        }
    }

    @Override
    public void down(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("DROP TABLE IF EXISTS public.animals;");
        }
    }
}