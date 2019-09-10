package com.fallntic.jotaayumouride.Model;

import java.io.Serializable;
import java.util.List;

public class ListPDFObject implements Serializable {
    String documentID;
    List<UploadPdf> listPDF_Khassida;

    public ListPDFObject(String documentID, List<UploadPdf> listPDF_Khassida) {
        this.documentID = documentID;
        this.listPDF_Khassida = listPDF_Khassida;
    }

    public ListPDFObject() {
    }

    public String getDocumentID() {
        return documentID;
    }

    public void setDocumentID(String documentID) {
        this.documentID = documentID;
    }

    public List<UploadPdf> getListPDF_Khassida() {
        return listPDF_Khassida;
    }

    public void setListPDF_Khassida(List<UploadPdf> listPDF_Khassida) {
        this.listPDF_Khassida = listPDF_Khassida;
    }
}
