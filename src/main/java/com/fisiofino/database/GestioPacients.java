package com.fisiofino.database;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import com.fisiofino.model.MalaltiaPacient;
import com.fisiofino.model.Pacient;

public class GestioPacients {

//Apartat 1: Insercions a partir del XML
    public int CreatePatient(Pacient p) throws SQLException {

        int idPacient = 0;

        Pacient existent = findBydniNie(p.getDni());
        if(existent != null) {
            return existent.getIdPatient();
        }
        String sql = "INSERT INTO patient (given_name, family_name, nif, birth_date, gender, " 
                    + "address, city, postal_code, country, telephone, email) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                    + "RETURNING id_patient";
        
        try (Connection con = Connexio.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, p.getGivenName());
                ps.setString(2, p.getFamilyName());
                ps.setString(3, p.getDni());
                ps.setObject(4, p.getBirthDate());
                ps.setString(5, p.getGender());
                ps.setString(6, p.getAddress());
                ps.setString(7, p.getCity());
                ps.setString(8, p.getPostalCode());
                ps.setString(9, p.getCountry());
                ps.setString(10, p.getTelephone());
                ps.setString(11, p.getEmail());

                try (ResultSet rs = ps.executeQuery()) {
                    if(rs.next()) {
                        idPacient = rs.getInt("id_patient");
                    }
                }
            }
                        
        return idPacient;
    }

    public boolean CreateMalaltia(MalaltiaPacient mp) throws SQLException {

        boolean malaltiaCreada = false;

        if(findMalaltia(mp.getCode())) {
            return false;
        }

        String sql = "INSERT INTO disease (snomed_code, name_disease) VALUES (?, ?)";

        try (Connection con = Connexio.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, mp.getCode());
            ps.setString(2, mp.getName());
            
            malaltiaCreada = ps.executeUpdate() > 0;
        }
        
        return malaltiaCreada;
    }    

    public boolean CreateMalaltiaPacient(MalaltiaPacient mp) throws SQLException {

        boolean malaltiaPacientCreada = false;

        if (findMalaltiaPacient(mp.getDniPacient(), mp.getCode(), "Activo")
            || findMalaltiaPacient(mp.getDniPacient(), mp.getCode(), mp.getStatus())) {
                
                return false;
        }

        String sql = "INSERT INTO patient_disease (id_patient, snomed_code, status, treatment) "
                    + "SELECT id_patient, ?, ?, ? FROM patient WHERE nif = ?";
             
        try (Connection con = Connexio.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, mp.getCode());
            ps.setString(2, mp.getStatus());
            ps.setString(3, mp.getTreatment());
            ps.setString(4, mp.getDniPacient());
            
            malaltiaPacientCreada = ps.executeUpdate() > 0;
        }            
        
        return malaltiaPacientCreada;
    }    

//Apartat 2: Cerca de pacient per DNI
    public Pacient findBydniNie(String dni_nie) throws SQLException {
            
        Pacient p = null;

        String sql = "SELECT * FROM patient WHERE nif = ?";

        try (Connection con = Connexio.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, dni_nie);
            
            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next()) {
                    p = crearPacient(rs);
                }
            }
        }

        return  p;
    }

//Apartat 3: Malalties del pacient
    public List<MalaltiaPacient> findDiseaseBydniNie(String dni_nie) throws SQLException {
        List<MalaltiaPacient> malaltiespacient = new ArrayList<MalaltiaPacient>();

        String sql = "SELECT p.nif, p.given_name, p.family_name, d.snomed_code, d.name_disease, "
                        + "pd.status, pd.treatment "
                        + "FROM patient p "
                        + "JOIN patient_disease pd ON p.id_patient = pd.id_patient "
                        + "JOIN disease d ON pd.snomed_code = d.snomed_code "
                        + "WHERE p.nif = ? "
                        + "ORDER BY pd.id_diagnosis";
                        
        try (Connection con = Connexio.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, dni_nie);  
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MalaltiaPacient mp = new MalaltiaPacient(
                        rs.getString("nif"),
                        rs.getString("given_name"),
                         rs.getString("family_name"),
                            rs.getString("snomed_code"),
                            rs.getString("name_disease"),
                            rs.getString("status"),
                            rs.getString("treatment"));
                    malaltiespacient.add(mp);        
                }
            }
        }                

        return malaltiespacient;
    }

//Apartat 4: Llistar tots els pacients
    public List<Pacient> findAllPacients () throws SQLException {
        List<Pacient> pacients = new ArrayList<>();

        String sql = "SELECT * FROM patient ORDER BY id_patient";

        try (Connection con = Connexio.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                pacients.add(crearPacient(rs));
            }    
        }
        
        return pacients;
    }

//Apartat 5: Modificar l'estat d'una malaltia d'un pacient
    public boolean updateStatus (int idPacient, String snomedCode, String status) throws SQLException {
        boolean modificat = false;

        String sql = "UPDATE patient_disease SET status = ? "
                    + "WHERE id_diagnosis = (SELECT MAX(id_diagnosis) FROM patient_disease "
                    + "                      WHERE id_patient = ? AND snomed_code = ?)";

        try (Connection con = Connexio.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, status);
            ps.setInt(2, idPacient);
            ps.setString(3, snomedCode);    
        }            

        return  modificat;
    }

// funcions i mètodes accesoris i necessaris per saber si s'ha d'insertar o modificar    
    public boolean findMalaltia (String SnomedCode) throws SQLException {
        boolean esMalaltia = false;
        
        String sql = "SELECT 1 FROM disease WHERE snomed_code = ?";

        try (Connection con = Connexio.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, SnomedCode);
            
            try (ResultSet rs = ps.executeQuery()) {
                esMalaltia = rs.next();
            }
        }
        
        return esMalaltia;
    }

   public boolean findMalaltiaPacient (String dniPacient, String SnomedCode, String status) throws SQLException {
        boolean esMalaltiaPacient = false;

        String sql = "SELECT 1 FROM patient_disease pd "
                   + "JOIN patient p ON pd.id_patient = p.id_patient "
                   + "WHERE p.nif = ? AND pd.snomed_code = ? AND pd.status = ?";

        try (Connection con = Connexio.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, dniPacient);
            ps.setString(2, SnomedCode);
            ps.setString(3, status);

            try (ResultSet rs = ps.executeQuery()) {
                esMalaltiaPacient = rs.next();
            }
        }

        return esMalaltiaPacient;
    }
    
    private Pacient crearPacient(ResultSet rs) throws SQLException {
        return new Pacient(
             rs.getInt("id_patient"),
                rs.getString("given_name"),
                rs.getString("family_name"),
                rs.getString("nif"),
                rs.getObject("birth_date", LocalDate.class),
                rs.getString("gender"),
                rs.getString("address"),
                rs.getString("city"),
                rs.getString("postal_code"),
                rs.getString("country"),
                rs.getString("telephone"),
                rs.getString("email")
        );
    }

}
