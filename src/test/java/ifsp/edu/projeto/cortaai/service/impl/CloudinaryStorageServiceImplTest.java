package ifsp.edu.projeto.cortaai.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.cloudinary.utils.ObjectUtils;
import ifsp.edu.projeto.cortaai.dto.UploadResultDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para CloudinaryStorageServiceImpl")
class CloudinaryStorageServiceImplTest {

    @Mock
    private Cloudinary cloudinary; // SDK principal (dependência)

    @Mock
    private Uploader uploader; // SDK aninhado (o que faz o trabalho)

    @InjectMocks
    private CloudinaryStorageServiceImpl storageService; // Classe sob teste

    private MockMultipartFile mockFile;
    private final String folder = "test-folder";
    private final String publicId = "test-folder/some-uuid";
    private final String secureUrl = "https://res.cloudinary.com/test/image/upload/v123/test-folder/some-uuid.jpg";

    @BeforeEach
    void setUp() {

        mockFile = new MockMultipartFile(
                "file",
                "image.jpg",
                "image/jpeg",
                "image-bytes".getBytes()
        );
    }

    @Test
    @DisplayName("uploadFile: Deve fazer upload e retornar UploadResultDTO com sucesso")
    void uploadFile_ShouldUploadAndReturnResult_WhenSuccessful() throws IOException {
        // Arrange
        // O mapa que o Cloudinary retornaria
        Map<String, Object> mockUploadResponse = Map.of(
                "secure_url", secureUrl,
                "public_id", publicId
        );

        when(cloudinary.uploader()).thenReturn(uploader);
        // Quando o uploader.upload for chamado com quaisquer bytes e um mapa de opções,
        // retorne a resposta mockada.
        // Usamos any() para os bytes e anyMap() para as opções.
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(mockUploadResponse);

        // Act
        UploadResultDTO result = storageService.uploadFile(mockFile, folder);

        // Assert
        assertNotNull(result);
        assertEquals(secureUrl, result.getSecureUrl());
        assertEquals(publicId, result.getPublicId());

        // Verifica se uploader.upload foi chamado exatamente 1 vez
        verify(uploader, times(1)).upload(eq(mockFile.getBytes()), anyMap());
    }

    @Test
    @DisplayName("uploadFile: Deve lançar IOException se o upload falhar")
    void uploadFile_ShouldThrowIOException_WhenUploadFails() throws IOException {
        // Arrange
        when(cloudinary.uploader()).thenReturn(uploader);
        // Simula a falha no upload lançando a exceção
        when(uploader.upload(any(byte[].class), anyMap())).thenThrow(new IOException("Falha na API"));

        // Act & Assert
        assertThrows(IOException.class, () -> {
            storageService.uploadFile(mockFile, folder);
        }, "Uma IOException deveria ser lançada pelo uploader.");
    }

    @Test
    @DisplayName("deleteFile: Deve chamar uploader.destroy com o publicId correto")
    void deleteFile_ShouldCallDestroy_WithCorrectPublicId() throws IOException {
        // Arrange
        String publicIdToDelete = "folder/id-to-delete";
        when(cloudinary.uploader()).thenReturn(uploader);
        // Simula o retorno de destroy (não precisamos dele, mas é boa prática)
        when(uploader.destroy(eq(publicIdToDelete), anyMap())).thenReturn(ObjectUtils.emptyMap());

        // Act
        storageService.deleteFile(publicIdToDelete);

        // Assert
        // Verifica se uploader.destroy foi chamado com o ID correto e as opções de invalidação
        verify(uploader, times(1)).destroy(eq(publicIdToDelete), eq(ObjectUtils.asMap("invalidate", true)));
    }

    @Test
    @DisplayName("deleteFile: Não deve fazer nada se publicId for nulo")
    void deleteFile_ShouldDoNothing_WhenPublicIdIsNull() throws IOException {
        // Act
        storageService.deleteFile(null);

        // Assert
        // Garante que o uploader.destroy NUNCA foi chamado
        verify(cloudinary, never()).uploader();
    }

    @Test
    @DisplayName("deleteFile: Não deve fazer nada se publicId estiver vazio")
    void deleteFile_ShouldDoNothing_WhenPublicIdIsEmpty() throws IOException {
        // Act
        storageService.deleteFile("");

        // Assert
        // Garante que o uploader.destroy NUNCA foi chamado
        verify(cloudinary, never()).uploader();
    }
}