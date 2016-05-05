package cz.muni.fi.pv168.libraryloans;

import java.util.List;
import javax.sql.DataSource;

/**
 *
 * @author L
 */
public interface BookManager {
    
    public void setDataSource(DataSource dataSource);
    
    public void createBook(Book book);
    
    public void updateBook(Book book);
    
    public void deleteBook(Book book);
    
    public Book getBookById(Long id);
    
    public List<Book> findAllBooks();
    
}
