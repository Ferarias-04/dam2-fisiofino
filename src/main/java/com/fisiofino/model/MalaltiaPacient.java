package com.fisiofino.model;

public class MalaltiaPacient {

    private String dniPacient;
    private String givenName;
    private String familyName;
    private String code;
    private String name;
    private String status;
    private String treatment;

    public MalaltiaPacient() {
    }
     public MalaltiaPacient(String dniPacient, String givenName, String familyName,
                           String code, String name, String status, String treatment) {
        this.dniPacient = dniPacient;
        this.givenName = givenName;
        this.familyName = familyName;
        this.code = code;
        this.name = name;
        this.status = status;
        this.treatment = treatment;
    }
    public String getDniPacient() { return dniPacient; }
    public void setDniPacient(String dniPacient) { this.dniPacient = dniPacient; }

    public String getGivenName() { return givenName; }
    public void setGivenName(String givenName) { this.givenName = givenName; }

    public String getFamilyName() { return familyName; }
    public void setFamilyName(String familyName) { this.familyName = familyName; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTreatment() { return treatment; }
    public void setTreatment(String treatment) { this.treatment = treatment; }

    @Override
    public String toString() {
        return "Pacient:     " + givenName + " " + familyName + " (" + dniPacient + ")\n"
             + "  Malaltia:  " + name + " [SNOMED " + code + "]\n"
             + "  Estat:     " + status + "\n"
             + "  Tractament: " + treatment;
    }
}
