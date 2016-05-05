package cz.muni.fi.pv168.web;

import cz.muni.fi.pv168.libraryloans.*;
import java.sql.SQLException;
import java.util.logging.Level;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.sql.DataSource;
import java.util.logging.Logger;

//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 *
 * @author L
 */
@WebListener
public class StartListener implements ServletContextListener {

    private final static Logger log = Logger.getLogger(StartListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent ev) {
        log.info("web app initialized");
        
        ServletContext servletContext = ev.getServletContext();
        try {
            DataSource dataSource = Main.createDatabase();
            BookManager bookManager = new BookManagerImpl();
            bookManager.setDataSource(dataSource);
            servletContext.setAttribute("bookManager", bookManager);
        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(StartListener.class.getName()).log(Level.SEVERE, "Cannot initialize database", ex);
        }
        log.info("manager was created and stored into servletContext attribute");
    }

    @Override
    public void contextDestroyed(ServletContextEvent ev) {
        log.info("application ends");
    }
}
