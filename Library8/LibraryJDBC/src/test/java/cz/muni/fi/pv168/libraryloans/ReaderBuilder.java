package cz.muni.fi.pv168.libraryloans;

/**
 * This is builder for the {@link Reader} class to make tests better readable.
 * @author L
 */
public class ReaderBuilder {
    
    private Long id;
    private String name;
    private String address;
    private String eMail;
    private String note;
    
    public ReaderBuilder id(Long id) {
        this.id = id;
        return this;
    }

    public ReaderBuilder name(String name) {
        this.name = name;
        return this;
    }

    public ReaderBuilder address(String address) {
        this.address = address;
        return this;
    }

    public ReaderBuilder eMail(String eMail) {
        this.eMail = eMail;
        return this;
    }

    public ReaderBuilder note(String note) {
        this.note = note;
        return this;
    }
    
    public Reader build() {
        Reader reader = new Reader();
        reader.setId(id);
        reader.setName(name);
        reader.setAddress(address);
        reader.setEMail(eMail);
        reader.setNote(note);
        return reader;
    }
}
