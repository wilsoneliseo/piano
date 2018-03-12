/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package principal;

/**
 *
 * @author wegt
 */
public class MiNota {
    private final double frecuencia;
    private final double duracion;
    
    public MiNota(double frec, double dur){
        this.frecuencia=frec;
        this.duracion=dur;
    }

   
    public double getFrecuencia() {
        return frecuencia;
    }

    /**
     * @return la duración de la nota
     */
    public double getDuracion() {
        return duracion;
    }
    
}
