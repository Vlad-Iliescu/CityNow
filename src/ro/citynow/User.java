package ro.citynow;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

public class User {
    private Integer id;
    private String adresa;
    private String zona;
    private String denumire;
    private Bitmap poza;

    public User(Integer id, String denumire, String zona, String adresa) {
        this.id = id;
        this.adresa = adresa;
        this.zona = zona;
        this.denumire = denumire;
    }

    public User(Integer id, String denumire, String zona, String adresa, String blob) {
        this.id = id;
        this.adresa = adresa;
        this.zona = zona;
        this.denumire = denumire;
        this.poza = makePoza(blob);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAdresa() {
        return adresa;
    }

    public void setAdresa(String adresa) {
        this.adresa = adresa;
    }

    public String getZona() {
        return zona;
    }

    public void setZona(String zona) {
        this.zona = zona;
    }

    public String getDenumire() {
        return denumire;
    }

    public void setDenumire(String denumire) {
        this.denumire = denumire;
    }

    public String getFullAdress() {
        String fullAdresa = "";
        if (zona != null && zona.length() > 0) {
            fullAdresa += zona + ", ";
        }

        if (adresa != null && adresa.length() > 0) {
            fullAdresa += adresa;
        }
        return fullAdresa;
    }

    private Bitmap makePoza(String blob) {
        if (blob == null || blob.length() <= 1) {
            return null;
        }
        byte[] decodedByte = Base64.decode(blob, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }

    public Bitmap getPoza() {
        return poza;
    }

    public void setPoza(Bitmap poza) {
        this.poza = poza;
    }

    public void setPoza(String blob) {
        this.poza = makePoza(blob);
    }

    @Override
    public String toString() {
        return String.format("<User {id=%d, denumire=\"%s\", zona=\"%s\" adresa=\"%s\"}>", id, denumire, zona, adresa);
    }
}
