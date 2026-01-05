package client.main;

import client.main.obj.ObjTriangleMeshFactory;
import javafx.scene.image.Image;
import javafx.scene.shape.TriangleMesh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssetLoader {
    private static final Logger logger = LoggerFactory.getLogger(AssetLoader.class);

    private final ObjTriangleMeshFactory objTriangleMeshFactory;

    public AssetLoader(ObjTriangleMeshFactory objTriangleMeshFactory) {
        if (objTriangleMeshFactory == null)
            throw new IllegalArgumentException("objTriangleMeshFactory is null");

        this.objTriangleMeshFactory = objTriangleMeshFactory;
    }

    public Map<String, Image> loadTextures(Map<String, String> texturesPaths, int textureWidth, int textureHeight) throws IOException {
        if (texturesPaths == null)
            throw new IllegalArgumentException("texturesPaths is null");
        if (textureWidth < 0)
            throw new IllegalArgumentException("textureWidth is negative");
        if (textureHeight < 0)
            throw new IllegalArgumentException("textureHeight is negative");

        Map<String, Image> textures = new HashMap<>();
        for (Map.Entry<String, String> entry : texturesPaths.entrySet()) {
            String name = entry.getKey();
            String path = entry.getValue();
            Image texture = loadTexture(path, textureWidth, textureHeight);
            textures.put(name, texture);
        }
        return textures;
    }

    public Image[] loadTextureArray(String folderPath, int textureWidth, int textureHeight) throws IOException {
        if (folderPath == null)
            throw new IllegalArgumentException("folderPath is null");
        if (textureWidth < 0)
            throw new IllegalArgumentException("textureWidth is negative");
        if (textureHeight < 0)
            throw new IllegalArgumentException("textureHeight is negative");

        List<Image> textures = new ArrayList<>();
        for (int i = 0; true; ++i) {
            String fileName = String.format("%02d.png", i);
            String fullPath = folderPath + "/" + fileName;

            try (InputStream inputStream = getClass().getResourceAsStream(fullPath)) {
                if (inputStream == null)
                    break;

                var texture = new Image(inputStream, textureWidth, textureHeight, true, false);
                textures.add(texture);
            }
        }
        return textures.toArray(new Image[0]);
    }

    public Map<String, TriangleMesh> loadMeshes(Map<String, String> meshesPaths) throws IOException {
        if (meshesPaths == null)
            throw new IllegalArgumentException("meshesPaths is null");

        Map<String, TriangleMesh> meshes = new HashMap<>();
        for (Map.Entry<String, String> entry : meshesPaths.entrySet()) {
            String name = entry.getKey();
            String path = entry.getValue();
            TriangleMesh triangleMesh = loadTriangleMesh(path);
            meshes.put(name, triangleMesh);
        }
        return meshes;
    }

    private static Image loadTexture(String texturePath, int textureWidth, int textureHeight) throws IOException {
        if (texturePath == null)
            throw new IllegalArgumentException("texturePath is null");
        if (textureWidth < 0)
            throw new IllegalArgumentException("textureWidth is negative");
        if (textureHeight < 0)
            throw new IllegalArgumentException("textureHeight is negative");

        Image texture;
        try (InputStream inputStream = MainClient.class.getResourceAsStream(texturePath)) {
            if (inputStream == null)
                throw new FileNotFoundException("Texture file not found: " + texturePath);

            texture = new Image(inputStream, textureWidth, textureHeight, true, false);
        }
        return texture;
    }

    private TriangleMesh loadTriangleMesh(String objPath) throws IOException {
        if (objPath == null)
            throw new IllegalArgumentException("objPath is null");

        TriangleMesh triangleMesh;
        try (InputStream inputStream = MainClient.class.getResourceAsStream(objPath)) {
            if (inputStream == null)
                throw new FileNotFoundException("OBJ file not found: " + objPath);

            var objContent = new String(inputStream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            triangleMesh = objTriangleMeshFactory.createTriangleMesh(objContent);
        }
        return triangleMesh;
    }
}