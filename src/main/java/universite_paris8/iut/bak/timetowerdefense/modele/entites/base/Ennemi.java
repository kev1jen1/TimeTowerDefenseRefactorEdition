package universite_paris8.iut.bak.timetowerdefense.modele.entites.base;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Point2D;
import universite_paris8.iut.bak.timetowerdefense.modele.entites.statiques.Defense;

import java.util.Collections;
import java.util.List;

public class Ennemi extends Entite implements Destructible {
    private int pv, pvMax;
    private DoubleProperty pvProp;
    private IntegerProperty direction;
    private double vitesseBase;
    private double vitesseActuelle;
    private int recompense;

    private int dureeStunRestante = 0;
    private int dureeSlowRestante = 0;
    private int dureeBrulageRestante = 0;
    private int dureeSpeedBoostRestante = 0;
    private boolean shield = false;

    private List<Point2D> chemin;
    private int etapeActuelle;
    private final int TILE_SIZE = 64;

    private int degatsSurMur = 10;
    private int cadenceAttaque = 60;
    private int compteurAttaque = 0;
    private boolean estBloque = false;

    public Ennemi(double x, double y, int pv, double vitesseBase, int recompense, List<Point2D> chemin) {
        super(x, y);
        this.pv = pv;
        this.pvMax = pv;
        this.vitesseBase = vitesseBase;
        this.vitesseActuelle = vitesseBase;
        this.recompense = recompense;

        this.chemin = chemin;
        this.etapeActuelle = 0;
        this.pvProp = new SimpleDoubleProperty(pv);
        this.direction = new SimpleIntegerProperty(-1);
    }


    public Ennemi(int pv, double vitesseBase, int recompense, List<Point2D> chemin) {
        this(0.0, 0.0, pv, vitesseBase, recompense, chemin);
    }


    public Ennemi(int x, int y, List<Point2D> chemin) {
        this(x, y, 25, 2.0, 5, chemin);
    }



public void avancer() {
    gererBrulure();
    if (estEtourdiOuBloque()) {
        return;
    }
    calculerVitesseSelonStatuts();
    deplacerVersProchaineEtape();
}

    private void gererBrulure() {
        if (this.dureeBrulageRestante > 0) {
            this.dureeBrulageRestante--;
            if (this.dureeBrulageRestante % 10 == 0) {
                this.recevoirDegats(1);
            }
        }
    }

    private boolean estEtourdiOuBloque() {
        if (this.dureeStunRestante > 0) {
            this.dureeStunRestante--;
            this.vitesseActuelle = 0;
            return true;
        }
        if (this.estBloque) {
            this.estBloque = false;
            return true;
        }
        return false;
    }

    private void calculerVitesseSelonStatuts() {
        this.vitesseActuelle = this.vitesseBase;
        if (this.dureeSlowRestante > 0) {
            this.dureeSlowRestante--;
            this.vitesseActuelle *= 0.5;
        }
        if (this.dureeSpeedBoostRestante > 0) {
            this.dureeSpeedBoostRestante--;
            this.vitesseActuelle *= 1.3;
        }
    }

    private void deplacerVersProchaineEtape() {
        if (aAtteintLaBase()) {
            return;
        }
        Point2D caseCible = chemin.get(etapeActuelle);
        double cibleX = caseCible.getX() * TILE_SIZE;
        double cibleY = caseCible.getY() * TILE_SIZE;
        double distanceX = cibleX - this.getX();
        double distanceY = cibleY - this.getY();

        mettreAJourPosition(distanceX, distanceY);
        verifierArriveeEtape(cibleX, cibleY, distanceX, distanceY);
    }

    private void mettreAJourPosition(double distanceX, double distanceY) {
        if (distanceX > 0) {
            this.setX(this.getX() + vitesseActuelle);
            this.direction.set(-1);
        } else if (distanceX < 0) {
            this.setX(this.getX() - vitesseActuelle);
            this.direction.set(1);
        }

        if (distanceY > 0) {
            this.setY(this.getY() + vitesseActuelle);
        } else if (distanceY < 0) {
            this.setY(this.getY() - vitesseActuelle);
        }
    }

    private void verifierArriveeEtape(double cibleX, double cibleY, double dx, double dy) {
        if (vitesseActuelle > 0 && Math.abs(dx) <= vitesseActuelle && Math.abs(dy) <= vitesseActuelle) {
            this.setX(cibleX);
            this.setY(cibleY);
            etapeActuelle++;
        }
    }

    public void agir(List<Ennemi> ennemis, List<Defense> defenses) {
    }

    public void seHeurterA(Destructible mur) {
        this.estBloque = true;

        if (compteurAttaque >= cadenceAttaque) {
            mur.recevoirDegats(this.degatsSurMur);
            compteurAttaque = 0;
            System.out.println(this.getClass().getSimpleName() + " tape la porte !");
        } else {
            compteurAttaque++;
        }
    }

    public void recevoirDegats(int dgt) {
        this.pv -= dgt;
        if (this.pv < 0) this.pv = 0;
        this.pvProp.set(pv);
    }

    public boolean estMort() {
        return this.pv <= 0;
    }

    public boolean aAtteintLaBase() {
        return chemin != null && etapeActuelle >= chemin.size();
    }
    
//     Méthode appelée à la mort de l'ennemi.
//     À Override pour les ennemis qui génèrent d'autres entités en mourant.

    public List<Ennemi> onDeath() {
        return Collections.emptyList();
    }

    public int getPv() {
        return pv;
    }

    public void ajouterPv(int pv) {
        this.pv += pv;
    }

    public int getPvMax() {
        return pvMax;
    }

    public IntegerProperty getDirectionProperty() {
        return direction;
    }

    public DoubleProperty getPvPropProperty() {
        return pvProp;
    }

    public int getRecompense() {
        return recompense;
    }

    public int getEtapeActuelle() {
        return etapeActuelle;
    }

    public void setEtapeActuelle(int etapeActuelle) {
        this.etapeActuelle = etapeActuelle;
    }

    private double calculerDemiTuile() {
        return TILE_SIZE / 2.0;
    }

    public double getCentreX() {
        return this.getX() + calculerDemiTuile();
    }

    public double getCentreY() {
        return this.getY() + calculerDemiTuile();
    }

    public List<Point2D> getChemin() {
        return chemin;
    }

    public double getVitesseBase() {
        return vitesseBase;
    }

    public void setVitesseBase(double vitesseBase) {
        this.vitesseBase = vitesseBase;
    }

    public void setVitesseActuelle(double vitesseActuelle) {
        this.vitesseActuelle = vitesseActuelle;
    }

    public void setPv(int pv) {
        this.pv = pv;
    }

    public void setRecompense(int recompense) {
        this.recompense = recompense;
    }

//     Applique un effet de statut à cet ennemi pour une durée donnée.
//     Renouvelle la durée si l'effet est déjà actif.
    public void appliqueEffet(Effet effet) {
        switch (effet) {
            case STUN -> {
                this.dureeStunRestante = Math.max(this.dureeStunRestante, 120);
                this.vitesseActuelle = 0;
            }
            case SLOW -> {
                this.dureeSlowRestante = Math.max(this.dureeSlowRestante, 120);
            }
            case BURN -> {
                this.dureeBrulageRestante = Math.max(this.dureeBrulageRestante, 60);
            }
            case SHIELD -> {
                if (!shield) {
                    this.ajouterPv(((int) Math.floor(this.getPv() * 1.2)));
                    shield = true;
                }
            }
            case SPEED_BOOST -> {
                this.dureeSpeedBoostRestante = Math.max(this.dureeSpeedBoostRestante, 150);
            }
        }
    }
}
