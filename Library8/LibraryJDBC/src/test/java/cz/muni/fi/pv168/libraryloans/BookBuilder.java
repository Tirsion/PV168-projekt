/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.pv168.libraryloans;

/**
 *
 * @author marcela
 */
public class BookBuilder {
    
    private Long id;
    private String title;
    private String author;
    private int published;
    private String note;

    public BookBuilder id(Long id) {
        this.id = id;
        return this;
    }

    public BookBuilder title(String title) {
        this.title = title;
        return this;
    }

    public BookBuilder author(String author) {
        this.author = author;
        return this;
    }

    public BookBuilder published(int published) {
        this.published = published;
        return this;
    }

    public BookBuilder note(String note) {
        this.note = note;
        return this;
    }

    public Book build() {
        Book book = new Book();
        book.setId(id);
        book.setTitle(title);
        book.setAuthor(author);
        book.setPublished(published);
        book.setNote(note);
        return book;
    }
}
