package com.fisiofino.controlador;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.fisiofino.LectorXML;
import com.fisiofino.database.GestioPacients;
import com.fisiofino.model.MalaltiaPacient;
import com.fisiofino.model.Pacient;
import com.fisiofino.vista.MenuView;

public class Controller {

    private final MenuView vista;
    private final GestioPacients gestiopacients;

    public Controller(MenuView vista, GestioPacients gestiopacients) {
        this.vista = vista;
        this.gestiopacients = gestiopacients;
    }

    public void inicia() {
        vista.mostraBenvinguda();
        boolean sortir = false;

        while (!sortir) {
            vista.MenuOpcions();
            try {
                String opcio = vista.demanaOpcio();
                if (opcio == null) break;

                switch (opcio.toUpperCase()) {
                    case "1": carregarXml();          break;
                    case "2": consultarInfoPersonal(); break;
                    case "3": consultarInfoMedica();   break;
                    case "4": consultarTots();         break;
                    case "5": modificarEstat();        break;
                    case "X": sortir = true;           break;
                    default:  vista.mostraError("Opció no vàlida");
                }
            } catch (IOException e) {
                vista.mostraError("Error de lectura: " + e.getMessage());
                sortir = true;
            } catch (Exception e) {
                vista.mostraError(e.toString());
            }
        }

        vista.mostraMissatge("Fins aviat!");
    }

    private void carregarXml() throws IOException, SQLException, ParserConfigurationException, SAXException {

        String ruta = vista.demana("Ruta del fitxer XML (Enter = pacientes_bluebutton.xml): ");
        if (ruta == null || ruta.isEmpty()) {
            ruta = "pacientes_bluebutton.xml";
        }

        Path fitxer = Path.of(ruta);
        if (!Files.exists(fitxer)) {
            vista.mostraError("No existeix el fitxer " + fitxer.toAbsolutePath());
            return;
        }

        // 1. Pacients: inserim només els que no existeixen
        int pacientsNous = 0;
        List<Pacient> pacients = LectorXML.llegirPacients(fitxer);
        for (Pacient p : pacients) {
            if (gestiopacients.findBydniNie(p.getDni()) == null) {
                gestiopacients.CreatePatient(p);
                pacientsNous++;
            }
        }

        // 2. Malalties: primer al catàleg (disease) i després la relació amb el pacient (patient_disease)
        int malaltiesNoves = 0;
        int diagnosticsNous = 0;
        List<MalaltiaPacient> malalties = LectorXML.llegirMalalties(fitxer);
        for (MalaltiaPacient mp : malalties) {
            if (!gestiopacients.findMalaltia(mp.getCode())) {
                gestiopacients.CreateMalaltia(mp);
                malaltiesNoves++;
            }
            // CreateMalaltiaPacient ja valida si està activa o repetida
            if (gestiopacients.CreateMalaltiaPacient(mp)) {
                diagnosticsNous++;
            }
        }

        vista.mostraMissatge("Càrrega completada:");
        vista.mostraMissatge("  Pacients llegits: " + pacients.size() + " (nous: " + pacientsNous + ")");
        vista.mostraMissatge("  Malalties noves al catàleg: " + malaltiesNoves);
        vista.mostraMissatge("  Diagnòstics llegits: " + malalties.size() + " (nous: " + diagnosticsNous + ")");
    }

    /* Informació personal  */
    private void consultarInfoPersonal() throws IOException, SQLException {
        String dni = demanaDni();
        if (dni == null) return;

        Pacient p = gestiopacients.findBydniNie(dni);
        if (p == null) {
            vista.mostraError("No hi ha cap pacient amb DNI/NIE " + dni);
        } else {
            vista.mostraPacient(p);
        }
    }

    /*  Informació mèdica  */
    private void consultarInfoMedica() throws IOException, SQLException {
        String dni = demanaDni();
        if (dni == null) return;

        List<MalaltiaPacient> malalties = gestiopacients.findDiseaseBydniNie(dni);
        if (malalties.isEmpty()) {
            vista.mostraMissatge("El pacient " + dni + " no té malalties registrades (o no existeix)");
        } else {
            vista.mostraMalaltiesPacient(malalties);
        }
    }

    /* Tots els pacients  */
    private void consultarTots() throws SQLException {
        List<Pacient> pacients = gestiopacients.findAllPacients();
        if (pacients.isEmpty()) {
            vista.mostraMissatge("No hi ha cap pacient. Carrega primer l'XML (opció 1).");
        } else {
            vista.mostraPacients(pacients);
        }
    }

    /*  Modificar estat  */
    private void modificarEstat() throws IOException, SQLException {
        String dni = demanaDni();
        if (dni == null) return;

        // Primer busquem el pacient per obtenir la seva id
        Pacient p = gestiopacients.findBydniNie(dni);
        if (p == null) {
            vista.mostraError("No hi ha cap pacient amb DNI/NIE " + dni);
            return;
        }

        String snomed = vista.demana("Codi SNOMED de la malaltia: ");
        if (snomed == null || snomed.isEmpty()) {
            vista.mostraError("Cal indicar el codi SNOMED");
            return;
        }

        String status = vista.demana("Nou estat (Activo / Controlado / Resuelto): ");
        if (status == null || status.isEmpty()) {
            vista.mostraError("Cal indicar l'estat");
            return;
        }

        if (gestiopacients.updateStatus(p.getIdPatient(), snomed, status)) {
            vista.mostraMissatge("Estat modificat correctament");
        } else {
            vista.mostraError("El pacient no té cap malaltia amb codi " + snomed);
        }
    }

    /*  Mètode auxiliar  */
    // Demana un DNI/NIE i en valida el format: 8 xifres + lletra (DNI) o X/Y/Z + 7 xifres + lletra (NIE)
    private String demanaDni() throws IOException {
        String dni = vista.demana("DNI/NIE del pacient: ");
        if (dni == null) return null;

        dni = dni.toUpperCase();
        if (!dni.matches("[0-9]{8}[A-Z]|[XYZ][0-9]{7}[A-Z]")) {
            vista.mostraError("Format incorrecte. Exemple: 12345678A o X1234567A");
            return null;
        }
        return dni;
    }
}