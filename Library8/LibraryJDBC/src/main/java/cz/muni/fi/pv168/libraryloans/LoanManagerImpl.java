package cz.muni.fi.pv168.libraryloans;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import java.time.*;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;

/**
 *
 * @author L
 */
public class LoanManagerImpl implements LoanManager {

    private DataSource dataSource;
    private final Clock clock;
    
    private ReaderManager readerManager;
    private BookManager bookManager;

    public void setReaderManager(ReaderManager readerManager) {
        this.readerManager = readerManager;
    }

    public void setBookManager(BookManager bookManager) {
        this.bookManager = bookManager;
    }

    public ReaderManager getReaderManager() {
        return readerManager;
    }

    public BookManager getBookManager() {
        return bookManager;
    }

    public LoanManagerImpl(Clock clock) {
        this.clock = clock;
    }
    
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    private static final Logger logger = Logger.getLogger(
            ReaderManagerImpl.class.getName());
    
    private void checkDataSource() {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource is not set");
        }
    }
    
    @Override
    public void createLoan(Loan loan) throws ServiceFailureException {
        checkDataSource();
        validate(loan);
 
        if(loan.getId()!=null) {
            throw new IllegalEntityException("loan id should not be assigned prior saving");
        }
        
        Connection connection = null;
        PreparedStatement st = null;
        
         try{
            connection = dataSource.getConnection();
            //Manual transaction control.
            connection.setAutoCommit(false);
            st = connection.prepareStatement(
                    "INSERT INTO LOAN (readerid,bookid,starttime,endtime,realend) VALUES (?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            
            st.setLong(1, loan.getReader().getId());
            st.setLong(2, loan.getBook().getId());
            st.setDate(3, toSqlDate(loan.getStartDate()));
            st.setDate(4, toSqlDate(loan.getExpectedEndDate()));
            st.setTimestamp(5, toSqlTimestamp(loan.getRealEndTime()));
            
            int addedRows = st.executeUpdate();
            DBUtils.checkUpdatesCount(addedRows, loan, true);
            
            Long id = DBUtils.getId(st.getGeneratedKeys());
            loan.setId(id);
            connection.commit();        
         } 
         catch (SQLException ex) {
            String msg = "Error when inserting loan " + loan;
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        }
        finally {
            DBUtils.doRollbackQuietly(connection);
            DBUtils.closeQuietly(connection, st);
        }
    }
    
    @Override
    public void updateLoan(Loan loan) throws ServiceFailureException {
        checkDataSource();
        validate(loan);
 
        if(loan.getId() == null) {
            throw new IllegalEntityException("loan id is null");
        }
        
        Connection connection = null;
        PreparedStatement st = null;
        
         try{
            connection = dataSource.getConnection();
            //Manual transaction control.
            connection.setAutoCommit(false);
            st = connection.prepareStatement(
                    "UPDATE loan SET readerid = ?, bookid = ?, starttime = ?, endtime = ?, realend = ? WHERE id = ?");
            
            st.setLong(1, loan.getReader().getId());
            st.setLong(2, loan.getBook().getId());
            st.setDate(3, toSqlDate(loan.getStartDate()));
            st.setDate(4, toSqlDate(loan.getExpectedEndDate()));
            st.setTimestamp(5, toSqlTimestamp(loan.getRealEndTime()));
            st.setLong(6, loan.getId());
            
            int addedRows = st.executeUpdate();
            DBUtils.checkUpdatesCount(addedRows, loan, false);
            connection.commit();        
         } 
         catch (SQLException ex) {
            String msg = "Error when updating loan " + loan;
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        }
        finally {
            DBUtils.doRollbackQuietly(connection);
            DBUtils.closeQuietly(connection, st);
        }
    }

    @Override
    public void deleteLoan(Loan loan) throws ServiceFailureException {
        if (loan == null) {
            throw new IllegalArgumentException("loan is null");
        }
        if (loan.getId() == null) {
            throw new IllegalEntityException("loan id is null");
        }
        
        Connection connection = null;
        PreparedStatement st = null;
        try{
            connection = dataSource.getConnection();
            //Manual transaction control.
            connection.setAutoCommit(false);
            st = connection.prepareStatement(
                    "DELETE FROM loan WHERE id = ?");
            
            st.setLong(1, loan.getId());
            
            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, loan, false);
            connection.commit();
        } catch (SQLException ex) {
            String msg = "Error when deleting loan" + loan;
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(connection);
            DBUtils.closeQuietly(connection, st);
        }
    }

    @Override
    public Loan getLoanById(Long id) throws ServiceFailureException {
        checkDataSource();
        
        if (id == null){
            throw new IllegalArgumentException("id is null");
        }
        Connection connection = null;
        PreparedStatement st = null;
        
        try {
            connection = dataSource.getConnection();
            st = connection.prepareStatement(
                    "SELECT id, readerid, bookid, starttime, endtime, realend " +
                    "FROM Loan WHERE id = ?");
            st.setLong(1, id);
            return executeQueryForSingleLoan(st);
        } catch (SQLException ex) {
            String msg = "Error when retrieving loan with id = " + id;
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);     
        } finally {
            DBUtils.closeQuietly(connection, st);
        }
    }

    @Override
    public List<Loan> findAllLoans() {
        checkDataSource();
        Connection connection = null;
        PreparedStatement st = null;
        try {
            connection = dataSource.getConnection();
            st = connection.prepareStatement(
                    "SELECT id, readerid, bookid, starttime, endtime, realend " +
                    "FROM Loan");
            return executeQueryForMultipleLoans(st);
        } catch (SQLException ex) {
            String msg = "Error when retrieving all loans";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);     
        } finally {
            DBUtils.closeQuietly(connection, st);
        }
    }

    @Override
    public List<Loan> findAllLoansForReader(Reader reader) {
        checkDataSource();
        Connection connection = null;
        PreparedStatement st = null;
        try {
            connection = dataSource.getConnection();
            st = connection.prepareStatement(
                    "SELECT id, readerid, bookid, starttime, endtime, realend " +
                    "FROM Loan WHERE readerid = ?");
            st.setLong(1, reader.getId());
            return executeQueryForMultipleLoans(st);
        } catch (SQLException ex) {
            String msg = "Error when retrieving loans for reader" + reader;
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);     
        } finally {
            DBUtils.closeQuietly(connection, st);
        }
    }

    @Override
    public List<Loan> findAllLoansForBook(Book book) {
        checkDataSource();
        Connection connection = null;
        PreparedStatement st = null;
        try {
            connection = dataSource.getConnection();
            st = connection.prepareStatement(
                    "SELECT id, readerid, bookid, starttime, endtime, realend " +
                    "FROM Loan WHERE bookid = ?");
            st.setLong(1, book.getId());
            return executeQueryForMultipleLoans(st);
        } catch (SQLException ex) {
            String msg = "Error when retrieving loans for book" + book;
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);     
        } finally {
            DBUtils.closeQuietly(connection, st);
        }
    }
    
    private static Date toSqlDate(LocalDate localDate) {
        return localDate == null ? null : Date.valueOf(localDate);
    }

    private static LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }
    
    private static Timestamp toSqlTimestamp(LocalDateTime localDateTime) {
        return localDateTime == null ? null : Timestamp.valueOf(localDateTime);
    }

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
    
    private Loan executeQueryForSingleLoan(PreparedStatement st) throws SQLException, ServiceFailureException {
        ResultSet rs = st.executeQuery();
        if (rs.next()) {
            Loan result = resultSetToLoan(rs);                
            if (rs.next()) {
                throw new ServiceFailureException(
                        "Internal integrity error: more loans with the same id found");
            }
            return result;
        } else {
            return null;
        }
    }
    
    private List<Loan> executeQueryForMultipleLoans(PreparedStatement st) throws SQLException {
        ResultSet rs = st.executeQuery();
        List<Loan> result = new ArrayList<>();
        while (rs.next()) {
            result.add(resultSetToLoan(rs));
        }
        return result;
    }
    
    private Loan resultSetToLoan(ResultSet rs) throws SQLException { 
        
               
       Loan loan = new Loan();
       loan.setId(rs.getLong("id"));
       loan.setReader(readerManager.getReaderById(rs.getLong("readerid")));
       loan.setBook(bookManager.getBookById(rs.getLong("bookid")));
       loan.setStartDate(toLocalDate(rs.getDate("starttime")));
       loan.setExpectedEndDate(toLocalDate(rs.getDate("endtime")));
       loan.setRealEndTime(toLocalDateTime(rs.getTimestamp("realend")));
       
       return loan;
    }
    
    private void validate(Loan loan) {
        if (loan == null) {
            throw new IllegalArgumentException("loan is null");
        }
        if (loan.getReader() == null) {
            throw new ValidationException("reader is null");
        }
        if (loan.getBook() == null) {
            throw new ValidationException("book is null");
        }
        if (loan.getStartDate() != null && loan.getExpectedEndDate() != null && loan.getExpectedEndDate().isBefore(loan.getStartDate())) {
            throw new ValidationException("expected end date is before start date");
        }
        LocalDate today = LocalDate.now(clock);
        if (loan.getStartDate() != null && loan.getStartDate().isAfter(today)) {
            throw new ValidationException("start date is in future");
        }
    }
}
