package cz.muni.fi.pv168.libraryloans;

import java.util.List;

/**
 *
 * @author L
 */
public interface ReaderManager {
    
    public void createReader (Reader reader);
    
    public void updateReader (Reader reader);
    
    public void deleteReader (Reader reader);
    
    public Reader getReaderById(Long id);
    
    public List<Reader> findReadersByName(String name);
    
    public List<Reader> findAllReaders();
    
}
