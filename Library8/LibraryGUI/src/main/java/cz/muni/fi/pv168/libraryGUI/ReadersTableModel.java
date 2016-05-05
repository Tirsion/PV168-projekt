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
public class ReadersTableModel extends AbstractTableModel {

    private List<Reader> readers = new ArrayList<>();
    
    public ReadersTableModel(ReaderManagerImpl manager) {
        this.readers = manager.findAllReaders();
    }

    @Override
    public int getRowCount() {
        return readers.size();
    }

    @Override
    public int getColumnCount() {
        return 5;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Reader reader = readers.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return reader.getId();
            case 1:
                return reader.getName();
            case 2:
                return reader.getAddress();
            case 3:
                return reader.getEMail();
            case 4:
                return reader.getNote();
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
                return "Name";
            case 2:
                return "Address";
            case 3:
                return "E-mail";
            case 4:
                return "Note";
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }

}
