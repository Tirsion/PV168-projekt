package cz.muni.fi.pv168.libraryloans;

import java.sql.SQLException;
import static java.time.Clock.systemDefaultZone;
import java.util.logging.Logger;

import javax.sql.DataSource;
import java.util.List;
import org.apache.derby.jdbc.EmbeddedDataSource;

/**
 *
 * @author L
 */
public class Main {
    
    private static final Logger log = Logger.getLogger(Main.class.getName());
    private static BookManagerImpl bookManager;
    private static ReaderManagerImpl readerManager;
    private static LoanManagerImpl loanManager;
    private static DataSource dataSource;
    

    public static DataSource prepareDataSource() {
        EmbeddedDataSource ds = new EmbeddedDataSource();
        ds.setDatabaseName("memory:librarymanager");
        ds.setCreateDatabase("create");
        return ds;
    }
    
    public static DataSource createDatabase() throws SQLException {
        dataSource = prepareDataSource();
        DBUtils.executeSqlScript(dataSource, Main.class.getResource("createTables.sql"));
        DBUtils.executeSqlScript(dataSource, Main.class.getResource("testData.sql"));
        return dataSource;
    }
    
    public static void dropDatabase() throws SQLException {
        DBUtils.executeSqlScript(dataSource, Reader.class.getResource("dropTables.sql"));
    }
    
    public static void main(String[] args) throws SQLException {

        log.info("start");
        
        createDatabase();
        bookManager = new BookManagerImpl();
        bookManager.setDataSource(dataSource);
        
        readerManager = new ReaderManagerImpl();
        readerManager.setDataSource(dataSource);
        
        loanManager = new LoanManagerImpl(systemDefaultZone());
        loanManager.setDataSource(dataSource);
        

        List<Book> allBooks = bookManager.findAllBooks();
        List<Reader> allReaders = readerManager.findAllReaders();

        System.out.println("allBooks = " + allBooks);
        System.out.println("allReaders = " + allReaders);
        
//        List<Loan> allLoans = loanManager.findAllLoans();
//        System.out.println("allLoans = " + allLoans);
        System.out.println("Reader with id 1: " + readerManager.getReaderById(1L));
        
        dropDatabase();
    }
}
