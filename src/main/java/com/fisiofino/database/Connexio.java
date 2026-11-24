package com.fisiofino.database;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/****AQUESTA CLASSE DEFINEIX LA CONNEXIÓ I EL DATASOURCE */

public class Connexio {
    private static HikariDataSource dataSource; 

    public static void iniciaConnexio () throws IOException {
       Properties props = new Properties();
       
       try (InputStream input = Connexio.class.getClassLoader()
                                                        .getResourceAsStream("db.properties")) {
            if (input == null ){
                throw new IOException("No s'ha trobat el fitxer db.properties");
            }
            props.load(input);                                                
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(props.getProperty("db.url"));
        config.setUsername(props.getProperty("db.user"));
        config.setPassword(props.getProperty("db.password"));
        config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.pool.size")));

        dataSource = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        if(dataSource == null) {
            throw new SQLException("La connexió no s'ha inicialitzat");
        }
        return dataSource.getConnection();
    }


    public static void tancaConnexio () {
    /************* HAS DE COMPLETAR EL CODI ******************/
    if(dataSource != null && !dataSource.isClosed()) {
        dataSource.close();
    }
    }  

}
