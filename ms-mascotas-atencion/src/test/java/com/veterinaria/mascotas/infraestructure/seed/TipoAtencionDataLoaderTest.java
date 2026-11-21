package com.veterinaria.mascotas.infraestructure.seed;

import com.veterinaria.mascotas.infraestructure.jpa_driver_adapter.TipoAtencionRepositoryJPA;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TipoAtencionDataLoaderTest {

    @Test
    void cargaLosCincoServiciosSiLaTablaEstaVacia() {
        TipoAtencionRepositoryJPA repo = mock(TipoAtencionRepositoryJPA.class);
        TipoAtencionDataLoader loader = new TipoAtencionDataLoader(repo);
        ReflectionTestUtils.setField(loader, "seedHabilitado", true);
        when(repo.count()).thenReturn(0L);
        loader.run();
        verify(repo).saveAll(anyList());
    }

    @Test
    void noCargaNadaSiYaHayDatos() {
        TipoAtencionRepositoryJPA repo = mock(TipoAtencionRepositoryJPA.class);
        TipoAtencionDataLoader loader = new TipoAtencionDataLoader(repo);
        ReflectionTestUtils.setField(loader, "seedHabilitado", true);
        when(repo.count()).thenReturn(5L);
        loader.run();
        verify(repo, never()).saveAll(anyList());
    }
}
