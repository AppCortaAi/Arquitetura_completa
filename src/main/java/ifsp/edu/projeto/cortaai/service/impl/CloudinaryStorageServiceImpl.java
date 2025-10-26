package ifsp.edu.projeto.cortaai.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import ifsp.edu.projeto.cortaai.service.StorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryStorageServiceImpl implements StorageService {

    private final Cloudinary cloudinary;

    // Injeta o Bean 'Cloudinary' que criamos no CloudinaryConfig
    public CloudinaryStorageServiceImpl(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public String uploadFile(MultipartFile file, String folder) throws IOException {

        // Gera um nome de arquivo único (public_id) para evitar colisões
        String publicId = folder + "/" + UUID.randomUUID().toString();

        // Faz o upload do arquivo
        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "public_id", publicId,
                        "folder", folder
                ));

        // Retorna a URL segura (https)
        return (String) uploadResult.get("secure_url");
    }
}