package com.desafio.service;

import com.desafio.entity.Aula;

public final class HorarioUtil {

    private HorarioUtil() {
    }

    public static boolean horariosSobrepostos(Aula a, Aula b) {
        if (a.getHorario() == null || b.getHorario() == null) {
            return false;
        }

        String diaA = a.getHorario().getDiaSemana();
        String diaB = b.getHorario().getDiaSemana();

        if (diaA == null || diaB == null || !diaA.equalsIgnoreCase(diaB)) {
            return false;
        }

        int inicioA = toMinutos(a.getHorario().getHoraInicio());
        int fimA = toMinutos(a.getHorario().getHoraFim());
        int inicioB = toMinutos(b.getHorario().getHoraInicio());
        int fimB = toMinutos(b.getHorario().getHoraFim());

        return inicioA < fimB && inicioB < fimA;
    }

    public static int toMinutos(String hora) {
        if (hora == null || hora.isBlank()) {
            throw new IllegalStateException("Horário inválido (nulo ou vazio)");
        }
        String[] partes = hora.split(":");
        if (partes.length != 2) {
            throw new IllegalStateException("Horário inválido: " + hora);
        }
        return Integer.parseInt(partes[0]) * 60 + Integer.parseInt(partes[1]);
    }
}