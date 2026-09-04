package com.desafio.dto;

import com.desafio.entity.Horario;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Horário de aula")
public class HorarioResponse {

    private Long id;
    private String diaSemana;
    private String horaInicio;
    private String horaFim;

    public static HorarioResponse from(Horario horario) {
        HorarioResponse response = new HorarioResponse();
        response.setId(horario.getId());
        response.setDiaSemana(horario.getDiaSemana());
        response.setHoraInicio(horario.getHoraInicio());
        response.setHoraFim(horario.getHoraFim());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(String diaSemana) {
        this.diaSemana = diaSemana;
    }

    public String getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(String horaInicio) {
        this.horaInicio = horaInicio;
    }

    public String getHoraFim() {
        return horaFim;
    }

    public void setHoraFim(String horaFim) {
        this.horaFim = horaFim;
    }
}