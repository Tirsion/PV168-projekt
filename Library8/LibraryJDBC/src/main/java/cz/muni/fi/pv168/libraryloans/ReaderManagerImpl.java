package cz.muni.fi.pv168.libraryloans;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;

/**
 *
 * @author L
 */
public class ReaderManagerImpl implements ReaderManager {
    
    private DataSource dataSource;
    
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
    public void createReader(Reader reader) throws ServiceFailureException {
        
        checkDataSource();
        validate(reader);
        if(reader.getId()!=null) {
            throw new IllegalEntityException("reader id should not be assigned prior saving");
        }
        
        Connection connection = null;
        PreparedStatement st = null;
        
        try{
            connection = dataSource.getConnection();
            //Manual transaction control.
            connection.setAutoCommit(false);
            st = connection.prepareStatement(
                    "INSERT INTO reader (name,address,email,note) VALUES (?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            
            st.setString(1, reader.getName());
            st.setString(2, reader.getAddress());
            st.setString(3, reader.getEMail());
            st.setString(4, reader.getNote());
            
            int addedRows = st.executeUpdate();
            DBUtils.checkUpdatesCount(addedRows, reader, true);
            
            Long id = DBUtils.getId(st.getGeneratedKeys());
            reader.setId(id);
            connection.commit();                        
        }
        catch (SQLException ex) {
            String msg = "Error when inserting reader " + reader;
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        }
        finally {
            DBUtils.doRollbackQuietly(connection);
            DBUtils.closeQuietly(connection, st);
        }
    }

    @Override
    public void updateReader(Reader reader) throws ServiceFailureException {
        checkDataSource();
        validate(reader);
        if(reader.getId() == null) {
            throw new IllegalEntityException("reader id is null");
        }
        
        Connection connection = null;
        PreparedStatement st = null;        
        try{
            connection = dataSource.getConnection();
            //Manual transaction control.
            connection.setAutoCommit(false);
            st = connection.prepareStatement(
                    "UPDATE reader SET name = ?, address = ?, email = ?, note = ? WHERE id = ?");
            
            st.setString(1, reader.getName());
            st.setString(2, reader.getAddress());
            st.setString(3, reader.getEMail());
            st.setString(4, reader.getNote());
            st.setLong(5, reader.getId());
            
            int addedRows = st.executeUpdate();
            DBUtils.checkUpdatesCount(addedRows, reader, false);
            connection.commit();
        } catch (SQLException ex) {
            String msg = "Error when updating reader " + reader;
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        }
        finally {
            DBUtils.doRollbackQuietly(connection);
            DBUtils.closeQuietly(connection, st);
        }
    }

    @Override
    public void deleteReader(Reader reader) throws ServiceFailureException {
        if (reader == null) {
            throw new IllegalArgumentException("reader is null");
        }
        if (reader.getId() == null) {
            throw new IllegalEntityException("reader id is null");
        }
        
        Connection connection = null;
        PreparedStatement st = null;
        try{
            connection = dataSource.getConnection();
            //Manual transaction control.
            connection.setAutoCommit(false);
            st = connection.prepareStatement(
                    "DELETE FROM reader WHERE id = ?");
            
            st.setLong(1, reader.getId());
            
            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, reader, false);
            connection.commit();
        } catch (SQLException ex) {
            String msg = "Error when deleting reader" + reader;
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(connection);
            DBUtils.closeQuietly(connection, st);
        }
    }

    @Override
    public Reader getReaderById(Long id) throws ServiceFailureException {
        checkDataSource();
        
        if (id == null){
            throw new IllegalArgumentException("id is null");
        }
        Connection connection = null;
        PreparedStatement st = null;
        
        try {
            connection = dataSource.getConnection();
            st = connection.prepareStatement(
                    "SELECT id,name,address,email,note FROM reader WHERE id = ?");
            
            st.setLong(1, id);
            return executeQueryForSingleReader(st);
        } catch (SQLException ex) {
            String msg = "Error when retrieving reader with id = " + id;
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);     
        } finally {
            DBUtils.closeQuietly(connection, st);
        }
    }
    
    @Override
    public List<Reader> findReadersByName(String name) throws ServiceFailureException {
        //NOT TESTED YET
        checkDataSource();
        Connection connection = null;
        PreparedStatement st = null;
        try {
            connection = dataSource.getConnection();
            st = connection.prepareStatement(
                    "SELECT id,name,address,email,note FROM reader WHERE name = ?");
            st.setString(1, name);
            return executeQueryForMultipleReaders(st);           
        } catch (SQLException ex) {
            String msg =  "Error when retrieving readers by name";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(connection, st);
        }
    }

    @Override
    public List<Reader> findAllReaders() throws ServiceFailureException {
        checkDataSource();
        Connection connection = null;
        PreparedStatement st = null;
        try {
            connection = dataSource.getConnection();
            st = connection.prepareStatement(
                    "SELECT id,name,address,email,note FROM reader");
            
            return executeQueryForMultipleReaders(st);           
        } catch (SQLException ex) {
            String msg =  "Error when retrieving all readers";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(connection, st);
        }
    }
    
    private void validate(Reader reader) throws IllegalArgumentException {
        if(reader==null) {
            throw new IllegalArgumentException("reader should not be null");
        }
        if(!reader.getName().matches("[a-žA-Ž]+[a-žA-Ž ]*")) {
            throw new IllegalArgumentException("name must contain at least one word character and no digits");
        }
        if (!reader.getEMail().contains("@")) {
            throw new IllegalArgumentException("eMail must contain @");
        }
    }
    
    private static Reader executeQueryForSingleReader(PreparedStatement st) throws SQLException, ServiceFailureException {
        ResultSet rs = st.executeQuery();
        if (rs.next()) {
            Reader result = resultSetToReader(rs);                
            if (rs.next()) {
                throw new ServiceFailureException(
                        "Internal integrity error: more readers with the same id found");
            }
            return result;
        } else {
            return null;
        }
    }
    
    private static List<Reader> executeQueryForMultipleReaders(PreparedStatement st) throws SQLException {
        ResultSet rs = st.executeQuery();
        List<Reader> result = new ArrayList<>();
        while (rs.next()) {
            result.add(resultSetToReader(rs));
        }
        return result;
    }
    
    private static Reader resultSetToReader(ResultSet rs) throws SQLException {
       Reader reader = new Reader();
       reader.setId(rs.getLong("id"));
       reader.setName(rs.getString("name"));
       reader.setAddress(rs.getString("address"));
       reader.setEMail(rs.getString("email"));
       reader.setNote(rs.getString("note"));
       return reader;
    }
}