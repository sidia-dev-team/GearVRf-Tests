package org.gearvrf.tester;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import net.jodah.concurrentunit.Waiter;

import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRShaderId;
import org.gearvrf.unittestutils.GVRSceneMaker;
import org.gearvrf.unittestutils.GVRTestUtils;
import org.gearvrf.unittestutils.GVRTestableActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

@RunWith(AndroidJUnit4.class)
public class RenderShaderTests
{
    private GVRTestUtils gvrTestUtils;
    private Waiter mWaiter;
    private boolean mDoCompare = false;

    public RenderShaderTests() {
        super();
    }

    @Rule
    public ActivityTestRule<GVRTestableActivity> ActivityRule = new
            ActivityTestRule<GVRTestableActivity>(GVRTestableActivity.class);


    @After
    public void tearDown()
    {
        GVRScene scene = gvrTestUtils.getMainScene();
        if (scene != null)
        {
            scene.clear();
        }
    }

    @Before
    public void setUp() throws TimeoutException {
        gvrTestUtils = new GVRTestUtils(ActivityRule.getActivity());
        mWaiter = new Waiter();
        gvrTestUtils.waitForOnInit();
    }

    private List<String> createMeshFormats() {
        final String type = "type: polygon";
        final String vertices = "vertices: [-0.5, 0.5, 0.0, -0.5, -0.5, 0.0, 0.5, 0.5, 0.0, 0.5, -0.5, 0.0]";
        final String normals = "normals: [0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0]";
        final String texcoords = "texcoords: [[0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 1.0, 1.0]]";
        final String triangles = "triangles: [ 0, 1, 2, 1, 3, 2 ]";

        final String weights = "bone_weights: [0.0, 1.0, 0.0, 0.0, 0.75, 0.25, 0.0, 0.0, 0.5, 0.5, 0.0, 0.0, 0.2, 0.2, 0.2, 0.4]";
        final String indices = "bone_indices: [ 0, 0, 0, 0, 0, 1, 0, 0, 1, 3, 0, 0, 0, 1, 3, 2 ]";

        List<String> meshFormats = new ArrayList<String>();

        // Mesh with positions, normals, texcoords
        meshFormats.add("{" + type + ", " + vertices + ", " + triangles + ", " + normals + ", "
                + texcoords + "}");
        // Mesh with positions, texcoords
        meshFormats.add("{" + type + ", " + vertices + ", " + triangles + ", " + texcoords + "}");
        // Mesh with positions, normals
        meshFormats.add("{" + type + ", " + vertices + ", " + triangles + ", " + normals + "}");
        // Mesh with positions, normals, texcoords, bone indices, bone weights
        meshFormats.add("{" + type + ", " + vertices + ", " + triangles + ", " + normals + ", "
                + texcoords + ", " + indices + ", " + weights + "}");
        // Mesh with positions, texcoords, bone indices, bone weights
        meshFormats.add("{" + type + ", " + vertices + ", " + triangles + ", " + texcoords + ", "
                + indices + ", " + weights + "}");
        // Mesh with positions, bone indices, bone weights
        meshFormats.add("{" + type + ", " + vertices + ", " + triangles + ", "
                + indices + ", " + weights + "}");

        return meshFormats;
    }

    private String createLightType(String type, float r, float g, float b, float x) {
        String ambientIntensity = "ambientintensity: {r:" + 0.3f * r + ", g:" + 0.3f * g
                + ", b:" + 0.3f * b + ", a: 1.0}";
        String diffuseIntensity = "diffuseintensity: {r:" + r + ", g:" + g + ", b:" + b
                + ", a: 1.0}";
        String specularIntensity = "specularintensity: {r:" + r + ", g:" + g + ", b:" + b
                + ", a: 1.0}";
        String position = "position: {x:" + x + ",y: 0.0, z: 0.0}";
        String rotation = "rotation: {w: 1.0, x: 0.0, y: 0.0, z: 0.0}";
        String spotCone = "innerconeangle: 20.0f, outerconeangle: 30.0f";

        return "{type:" + type + "," + rotation + ", " + position + ", " + spotCone + ","
                + ambientIntensity + ", " + diffuseIntensity + ", " + specularIntensity + "}";
    }

    private List<String> createLightingTypes() throws JSONException {
        List<String> lightingTypes = new ArrayList<String >();

        // No lighting
        lightingTypes.add("");

        // one directional light
        lightingTypes.add("["
                + createLightType("directional", 1.0f, 0.3f, 0.3f, 0.0f) + "]");
        // one spot light
        lightingTypes.add("["
                + createLightType("spot", 1.0f, 0.3f, 0.3f, 0.0f) + "]");
        // one point light
        lightingTypes.add("["
                + createLightType("point", 1.0f, 0.3f, 0.3f, 0.0f) + "]");
        // two directional lights
        lightingTypes.add("["
                + createLightType("directional", 1.0f, 0.3f, 0.3f, -1.0f) + ", "
                + createLightType("directional", 1.0f, 0.3f, 0.3f, 1.0f) + "]");
        // two spot lights
        lightingTypes.add("["
                + createLightType("spot", 1.0f, 0.3f, 0.3f, -1.0f) + ","
                + createLightType("spot", 1.0f, 0.3f, 0.3f, 1.0f) + "]");
        // two point lights
        lightingTypes.add("["
                + createLightType("point", 1.0f, 0.3f, 0.3f, -1.0f) + ","
                + createLightType("point", 1.0f, 0.3f, 0.3f, 1.0f) + "]");
        // one directional, one spot
        lightingTypes.add("["
                + createLightType("directional", 1.0f, 0.3f, 0.3f, -1.0f) + ", "
                + createLightType("spot", 1.0f, 0.3f, 0.3f, 1.0f) + "]");
        // one directional, one point
        lightingTypes.add("["
                + createLightType("directional", 1.0f, 0.3f, 0.3f, -1.0f) + ", "
                + createLightType("point", 1.0f, 0.3f, 0.3f, 1.0f) + "]");
        // one directional, one point, one spot
        lightingTypes.add("["
                + createLightType("directional", 1.0f, 0.3f, 0.3f, -1.0f) + ", "
                + createLightType("spot", 1.0f, 0.3f, 0.3f, 0.0f) + ", "
                + createLightType("point", 1.0f, 0.3f, 0.3f, 1.0f) + "]");

        return lightingTypes;
    }

    private String createMaterialFormat(GVRShaderId shaderId, int textureResourceID) {
        String materialFormat = "";

        String type = shaderId == GVRMaterial.GVRShaderType.Phong.ID ?
                "shader: phong," : "shader: texture,";

        if (textureResourceID == -1) {
            final String color = "color: {r: 0.0, g: 1.0, b: 0.0, a: 1.0}";
            materialFormat = "{ " + type + color + "}";
        } else {
            final String textureFormat = "textures: [{" +
                    "id: default, " +
                    "type: bitmap," +
                    "resource_id:" + textureResourceID + "}]";

            materialFormat = "{" + type + textureFormat + "}";
        }

        return materialFormat;
    }

    private String createMaterialFormat(GVRShaderId shaderId) {
        return createMaterialFormat(shaderId, -1);
    }

    private List<String> createMaterialFormats() {
        List<String> materials = new ArrayList<String>();

        materials.add(createMaterialFormat(GVRMaterial.GVRShaderType.Phong.ID));
        materials.add(createMaterialFormat(GVRMaterial.GVRShaderType.Texture.ID));
        materials.add(createMaterialFormat(GVRMaterial.GVRShaderType.Phong.ID, R.drawable.checker));
        materials.add(createMaterialFormat(GVRMaterial.GVRShaderType.Texture.ID, R.drawable.checker));

        return materials;
    }

    /**
     * Test mesh formats and shader combinations.
     *
     * @throws TimeoutException
     */
    @Test
    public void meshFormatsWithShaderCombinationsTest() throws TimeoutException {
        // Mesh with positions, normals, texcoords, bone indices, bone weights
        JSONObject jsonScene = null;
        List<String> meshFormats = null;
        List<String> materials = null;
        List<String> lightingTypes = null;
        String screenshotName = null;
        try {
            materials = createMaterialFormats();
            meshFormats = createMeshFormats();
            lightingTypes = createLightingTypes();

            for (int i = 0; i < meshFormats.size(); i++) { // Mesh formats
                for (int j = 0; j < lightingTypes.size(); j++) { // Lighting
                    for (int k = 0; k < materials.size(); k+= 2) { // Materials/Shaders
                        jsonScene = new JSONObject("{\"id\": \"scene" + i + "\"}");
                        JSONArray objects = new JSONArray();
                        JSONObject objectPhong = new JSONObject();
                        objectPhong.put("geometry", new JSONObject(meshFormats.get(i)));
                        objectPhong.put("material", new JSONObject(materials.get(k)));
                        objectPhong.put("position", new JSONObject("{x: -1.0f, z: -2.0}"));
                        objectPhong.put("scale", new JSONObject("{x: 2.0, y: 2.0, z: 2.0}"));

                        JSONObject objectTexture = new JSONObject();
                        objectTexture.put("geometry", new JSONObject(meshFormats.get(i)));
                        objectTexture.put("material", new JSONObject(materials.get(k + 1)));
                        objectTexture.put("position", new JSONObject("{x: 1.0f, z: -2.0}"));
                        objectTexture.put("scale", new JSONObject("{x: 2.0, y: 2.0, z: 2.0}"));

                        objects.put(objectPhong);
                        objects.put(objectTexture);

                        jsonScene.put("objects", objects);
                        if (j > 0) { // No lighting when j == 0
                            jsonScene.put("lights", new JSONArray(lightingTypes.get(j)));
                        }

                        GVRSceneMaker.makeScene(gvrTestUtils.getGvrContext(), gvrTestUtils.getMainScene(),
                                jsonScene);

                        gvrTestUtils.waitForSceneRendering();
                        screenshotName = "testMesh" + i + "Lighting" + j;
                        gvrTestUtils.screenShot(getClass().getSimpleName(), screenshotName, mWaiter, mDoCompare);

                        gvrTestUtils.getMainScene().clear();
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
