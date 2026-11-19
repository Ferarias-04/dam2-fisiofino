-- FiSioFiNo - Model relacional (Activitat RA2)

CREATE TABLE patient (
    id_patient   SERIAL PRIMARY KEY,
    given_name   VARCHAR(100) NOT NULL,
    family_name  VARCHAR(150) NOT NULL,
    nif          VARCHAR(15)  NOT NULL UNIQUE,
    birth_date   DATE,
    gender       VARCHAR(20),
    address      VARCHAR(255),
    city         VARCHAR(100),
    postal_code  VARCHAR(10),
    country      VARCHAR(100),
    telephone    VARCHAR(30),
    email        VARCHAR(150)
);

CREATE TABLE disease (
    snomed_code   VARCHAR(20)  PRIMARY KEY,
    name_disease  VARCHAR(255) NOT NULL
);

CREATE TABLE patient_disease (
    id_diagnosis  SERIAL PRIMARY KEY,
    id_patient    INTEGER     NOT NULL REFERENCES patient(id_patient) ON DELETE CASCADE,
    snomed_code   VARCHAR(20) NOT NULL REFERENCES disease(snomed_code),
    status        VARCHAR(30) NOT NULL,
    treatment     TEXT
);
