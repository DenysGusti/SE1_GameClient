package client.assets;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javafx.scene.shape.TriangleMesh;

class ObjTriangleMeshFactoryTest {
    private ObjTriangleMeshFactory factory;

    @BeforeEach
    void setUp() {
        factory = new ObjTriangleMeshFactory();
    }

    @Test
    void CreateTriangleMesh_NullContent_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> factory.createTriangleMesh(null));
    }

    @Test
    void CreateTriangleMesh_ValidContent_PopulatesMeshCorrectly() {
        String objContent = """
                 # Comment line to cover filter
                 v 1.0 2.0 3.0
                 vn 4.0 5.0 6.0
                 vt 0.7 0.2
                                \s
                 f 1/1/1 1/1/1 1/1/1
                \s""";

        TriangleMesh mesh = factory.createTriangleMesh(objContent);

        assertThat(mesh.getPoints().get(0), is(3.f));
        assertThat(mesh.getPoints().get(1), is(-2.f));
        assertThat(mesh.getPoints().get(2), is(1.f));

        assertThat(mesh.getNormals().get(0), is(6.f));
        assertThat(mesh.getNormals().get(1), is(-5.f));
        assertThat(mesh.getNormals().get(2), is(4.f));

        assertThat(mesh.getTexCoords().get(0), is(0.7f));
        assertThat(mesh.getTexCoords().get(1), is(0.8f));

        assertThat(mesh.getFaces().size(), is(9));
    }

    @Test
    void CreateTriangleMesh_PartialFaceIndices_CoversAllTernaryBranches() {
        // v/vt/vn mapping logic:
        // f 1/1/1 -> all present
        // f 1/1   -> vn defaults
        // f 1//1  -> vt defaults
        // f 1     -> vt/vn default
        // f 1/1/  -> vn defaults (just in case)

        String objContent = """
                v 1 1 1
                vt 0 0
                vn 0 0 1
                f 1/1/1 1/1/1 1/1/1
                f 1//1 1//1 1//1
                f 1/1 1/1 1/1
                f 1 1 1
                f 1/1/ 1/1/ 1/1/
                """;

        TriangleMesh mesh = factory.createTriangleMesh(objContent);

        // 5 faces * 3 vertices * 3 indices = 45 indices
        assertThat(mesh.getFaces().size(), is(45));

        assertThat(mesh.getFaces().get(11), is(0));
        assertThat(mesh.getFaces().get(28), is(0));
        assertThat(mesh.getFaces().get(37), is(0));
    }

    @Test
    void CreateTriangleMesh_UnknownPrefixAndEmptyLines_SkipsGracefully() {
        String objContent = """
                g GroupName
                s off
                                
                v 1 1 1
                """;

        TriangleMesh mesh = factory.createTriangleMesh(objContent);
        assertThat(mesh.getPoints().size(), is(3));
    }
}