/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.pv168.libraryGUI;

import cz.muni.fi.pv168.libraryloans.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author xskvari1
 */
public class BooksTableModel extends AbstractTableModel {

    private List<Book> books = new ArrayList<>();

    public BooksTableModel(BookManagerImpl manager) {
        this.books = manager.findAllBooks();
    }

//    public BooksTableModel() {
//    }
    @Override
    public int getRowCount() {
        return books.size();
    }

    @Override
    public int getColumnCount() {
        return 5;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Book book = books.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return book.getId();
            case 1:
                return book.getAuthor();
            case 2:
                return book.getTitle();
            case 3:
                return book.getPublished();
            case 4:
                return book.getNote();
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "Id";
            case 1:
                return "Author";
            case 2:
                return "Title";
            case 3:
                return "Published";
            case 4:
                return "Note";
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return Long.class;
            case 1:
            case 2:
            case 3:
            case 4:
                return String.class;
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }

}
