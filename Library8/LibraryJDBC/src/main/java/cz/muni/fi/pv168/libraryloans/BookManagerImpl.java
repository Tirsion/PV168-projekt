package cz.muni.fi.pv168.libraryloans;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.sql.DataSource;
import java.util.logging.Level;
/**
 *
 * @author L
 */
public class BookManagerImpl implements BookManager {
    
    private static final Logger logger = Logger.getLogger(
        BookManagerImpl.class.getName());
    
    private DataSource dataSource;

    @Override
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    private void checkDataSource() {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource is not set");
        }
    }

    @Override
    public void createBook(Book book) throws ServiceFailureException  {

        checkDataSource();
        validate(book);
        if (book.getId() != null) {
            throw new IllegalEntityException("book id is already set");
        }                
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            st = conn.prepareStatement(
                    "INSERT INTO Book (title,author,published,note) VALUES (?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);        
            st.setString(1, book.getTitle());
            st.setString(2, book.getAuthor());
            st.setInt(3, book.getPublished());
            st.setString(4, book.getNote());

            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, book, true);

            Long id = DBUtils.getId(st.getGeneratedKeys());
            book.setId(id);
            conn.commit();        
        } catch (SQLException ex) {
            String msg = "Error when inserting book into db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }
    }
    
    @Override
    public void updateBook(Book book) throws ServiceFailureException {
        checkDataSource();
        validate(book);
        if (book.getId() == null) {
            throw new IllegalEntityException("book id is null");
        }        
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            st = conn.prepareStatement(
                    "UPDATE Book SET title = ?, author = ?, published = ?, note = ? WHERE id = ?");
            st.setString(1, book.getTitle());
            st.setString(2, book.getAuthor());
            st.setInt(3, book.getPublished());
            st.setString(4, book.getNote());
            st.setLong(5, book.getId());
            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, book, false);
            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when updating book in the db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }
    }
    
    @Override
    public void deleteBook(Book book) throws ServiceFailureException {
        checkDataSource();
        if (book == null) {
            throw new IllegalArgumentException("book is null");
        }        
        if (book.getId() == null) {
            throw new IllegalEntityException("book id is null");
        }        
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            st = conn.prepareStatement(
                    "DELETE FROM Book WHERE id = ?");
            st.setLong(1, book.getId());

            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, book, false);
            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when deleting book from the db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }
    }
    
    @Override
    public Book getBookById(Long id) throws ServiceFailureException {

        checkDataSource();
        
        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }
        
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(
                    "SELECT id,title,author,published,note FROM Book WHERE id = ?");
            st.setLong(1, id);
            return executeQueryForSingleBook(st);
        } catch (SQLException ex) {
            String msg = "Error when getting book with id = " + id + " from DB";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }
    }    
    
    @Override
    public List<Book> findAllBooks() throws ServiceFailureException {
        checkDataSource();
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(
                    "SELECT id,title,author,published,note FROM Book");
            return executeQueryForMultipleBooks(st);
        } catch (SQLException ex) {
            String msg = "Error when getting all books from DB";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }          
    }    
    
    private static Book executeQueryForSingleBook(PreparedStatement st) throws SQLException, ServiceFailureException {
        ResultSet rs = st.executeQuery();
        if (rs.next()) {
            Book result = resultSetToBook(rs);                
            if (rs.next()) {
                throw new ServiceFailureException(
                        "Internal integrity error: more books with the same id found!");
            }
            return result;
        } else {
            return null;
        }
    }
    
    private static List<Book> executeQueryForMultipleBooks(PreparedStatement st) throws SQLException {
        ResultSet rs = st.executeQuery();
        List<Book> result = new ArrayList<>();
        while (rs.next()) {
            result.add(resultSetToBook(rs));
        }
        return result;
    }

    private static Book resultSetToBook(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setId(rs.getLong("id"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setPublished(rs.getInt("published"));
        book.setNote(rs.getString("note"));
        return book;
    }
    
    private void validate(Book book) throws IllegalArgumentException {
        if (book == null) {
            throw new IllegalArgumentException("book is null");
        }
        if (book.getTitle().length() == 0) {
            throw new IllegalArgumentException("empty field - title");
        }
        if(!book.getAuthor().matches("[a-žA-Ž]+[a-žA-Ž ]*")) {
            throw new IllegalArgumentException("name must contain at least one word character and no digits");
        }
        if (book.getAuthor().length() == 0) {
            throw new IllegalArgumentException("empty field - author");
        }
    }
}