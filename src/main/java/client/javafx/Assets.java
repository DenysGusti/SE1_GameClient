package client.javafx;

import client.data.XYPair;
import javafx.scene.image.Image;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

public class Assets {
    private static final Logger logger = LoggerFactory.getLogger(Assets.class);

    private final Map<String, PhongMaterial> materials = new HashMap<>();
    private final Map<String, TriangleMesh> meshes;
    private final Image[] waterTextures;

    private int waterMaterialIndex = 0;

    public Assets(Map<String, Image> textures, Image[] waterTextures, Map<String, TriangleMesh> meshes) {
        if (textures == null)
            throw new IllegalArgumentException("textures is null");
        if (waterTextures == null)
            throw new IllegalArgumentException("waterTextures is null");
        if (meshes == null)
            throw new IllegalArgumentException("meshes is null");

        this.waterTextures = waterTextures;
        this.meshes = meshes;

        textures.forEach((textureName, image) -> {
            var phongMaterial = new PhongMaterial();
            Image texture = Objects.requireNonNull(textures.get(textureName), "texture is null");
            phongMaterial.setDiffuseMap(texture);
            materials.put(textureName, phongMaterial);
        });

        materials.put("water", new PhongMaterial());
    }

    public void rotateWaterTexture() {
        waterMaterialIndex = (waterMaterialIndex + 1) % waterTextures.length;
        materials.get("water").setDiffuseMap(waterTextures[waterMaterialIndex]);
    }

    public PhongMaterial getMaterial(String materialName) {
        if (materialName == null)
            throw new IllegalArgumentException("materialName is null");

        if (!materials.containsKey(materialName))
            throw new NoSuchElementException("Material " + materialName + " not found");

        return materials.get(materialName);
    }

    public TriangleMesh getMesh(String meshName) {
        if (meshes == null)
            throw new IllegalArgumentException("meshes is null");

        if (!meshes.containsKey(meshName))
            throw new NoSuchElementException("Mesh " + meshName + " not found");

        return meshes.get(meshName);
    }

    public MeshView createMeshView(String meshName, String materialName, XYPair coordinate, double y) {
        if (meshName == null)
            throw new IllegalArgumentException("meshName is null");
        if (materialName == null)
            throw new IllegalArgumentException("materialName is null");
        if (coordinate == null)
            throw new IllegalArgumentException("coordinate is null");

        var meshView = new MeshView(getMesh(meshName));
        meshView.setMaterial(getMaterial(materialName));
        meshView.setCullFace(CullFace.BACK);
        meshView.setTranslateX(coordinate.y());
        meshView.setTranslateZ(coordinate.x());
        meshView.setTranslateY(y);
        return meshView;
    }
}
