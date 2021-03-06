package org.deeplearning4j.regressiontest;

import org.deeplearning4j.BaseDL4JTest;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.ConvolutionMode;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.graph.LayerVertex;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.GravesLSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.conf.layers.variational.VariationalAutoencoder;
import org.deeplearning4j.nn.conf.serde.legacyformat.LegacyLayerDeserializer;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.regressiontest.customlayer100a.CustomLayer;
import org.deeplearning4j.util.ModelSerializer;
import org.junit.Test;
import org.nd4j.linalg.activations.impl.*;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.io.ClassPathResource;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.learning.config.RmsProp;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

import static org.junit.Assert.*;

public class RegressionTest100a extends BaseDL4JTest {

    @Test
    public void testCustomLayer() throws Exception {

        File f = new ClassPathResource("regression_testing/100a/CustomLayerExample_100a.bin").getTempFileFromArchive();

        try {
            MultiLayerNetwork.load(f, true);
            fail("Expected exception");
        } catch (Exception e){
            String msg = e.getMessage();
            assertTrue(msg, msg.contains("NeuralNetConfiguration.registerLegacyCustomClassesForJSON"));
        }

        NeuralNetConfiguration.registerLegacyCustomClassesForJSON(CustomLayer.class);

        MultiLayerNetwork net = MultiLayerNetwork.load(f, true);

        DenseLayer l0 = (DenseLayer) net.getLayer(0).conf().getLayer();
        assertEquals(new ActivationTanH(), l0.getActivationFn());
        assertEquals(0.03, l0.getL2(), 1e-6);
        assertEquals(new RmsProp(0.95), l0.getIUpdater());

        CustomLayer l1 = (CustomLayer) net.getLayer(1).conf().getLayer();
        assertEquals(new ActivationTanH(), l1.getActivationFn());
        assertEquals(new ActivationSigmoid(), l1.getSecondActivationFunction());
        assertEquals(new RmsProp(0.95), l1.getIUpdater());


        INDArray outExp;
        File f2 = new ClassPathResource("regression_testing/100a/CustomLayerExample_Output_100a.bin").getTempFileFromArchive();
        try(DataInputStream dis = new DataInputStream(new FileInputStream(f2))){
            outExp = Nd4j.read(dis);
        }

        INDArray in;
        File f3 = new ClassPathResource("regression_testing/100a/CustomLayerExample_Input_100a.bin").getTempFileFromArchive();
        try(DataInputStream dis = new DataInputStream(new FileInputStream(f3))){
            in = Nd4j.read(dis);
        }

        INDArray outAct = net.output(in);

        assertEquals(outExp, outAct);


        //Check graph
        f = new ClassPathResource("regression_testing/100a/CustomLayerExample_Graph_100a.bin").getTempFileFromArchive();

        //Deregister custom class:
        new LegacyLayerDeserializer().getLegacyNamesMap().remove("CustomLayer");

        try {
            ComputationGraph.load(f, true);
            fail("Expected exception");
        } catch (Exception e){
            String msg = e.getMessage();
            assertTrue(msg, msg.contains("NeuralNetConfiguration.registerLegacyCustomClassesForJSON"));
        }

        NeuralNetConfiguration.registerLegacyCustomClassesForJSON(CustomLayer.class);

        ComputationGraph graph = ComputationGraph.load(f, true);

        f2 = new ClassPathResource("regression_testing/100a/CustomLayerExample_Graph_Output_100a.bin").getTempFileFromArchive();
        try(DataInputStream dis = new DataInputStream(new FileInputStream(f2))){
            outExp = Nd4j.read(dis);
        }

        outAct = graph.outputSingle(in);

        assertEquals(outExp, outAct);
    }


    @Test
    public void testGravesLSTM() throws Exception {

        File f = new ClassPathResource("regression_testing/100a/GravesLSTMCharModelingExample_100a.bin").getTempFileFromArchive();
        MultiLayerNetwork net = MultiLayerNetwork.load(f, true);

        GravesLSTM l0 = (GravesLSTM) net.getLayer(0).conf().getLayer();
        assertEquals(new ActivationTanH(), l0.getActivationFn());
        assertEquals(200, l0.getNOut());
        assertEquals(WeightInit.XAVIER, l0.getWeightInit());
        assertEquals(0.001, l0.getL2(), 1e-6);
        assertEquals(new RmsProp(0.1), l0.getIUpdater());

        GravesLSTM l1 = (GravesLSTM) net.getLayer(1).conf().getLayer();
        assertEquals(new ActivationTanH(), l1.getActivationFn());
        assertEquals(200, l1.getNOut());
        assertEquals(WeightInit.XAVIER, l1.getWeightInit());
        assertEquals(0.001, l1.getL2(), 1e-6);
        assertEquals(new RmsProp(0.1), l1.getIUpdater());

        RnnOutputLayer l2 = (RnnOutputLayer) net.getLayer(2).conf().getLayer();
        assertEquals(new ActivationSoftmax(), l2.getActivationFn());
        assertEquals(77, l2.getNOut());
        assertEquals(WeightInit.XAVIER, l2.getWeightInit());
        assertEquals(0.001, l0.getL2(), 1e-6);
        assertEquals(new RmsProp(0.1), l0.getIUpdater());

        assertEquals(BackpropType.TruncatedBPTT, net.getLayerWiseConfigurations().getBackpropType());
        assertEquals(50, net.getLayerWiseConfigurations().getTbpttBackLength());
        assertEquals(50, net.getLayerWiseConfigurations().getTbpttFwdLength());

        INDArray outExp;
        File f2 = new ClassPathResource("regression_testing/100a/GravesLSTMCharModelingExample_Output_100a.bin").getTempFileFromArchive();
        try(DataInputStream dis = new DataInputStream(new FileInputStream(f2))){
            outExp = Nd4j.read(dis);
        }

        INDArray in;
        File f3 = new ClassPathResource("regression_testing/100a/GravesLSTMCharModelingExample_Input_100a.bin").getTempFileFromArchive();
        try(DataInputStream dis = new DataInputStream(new FileInputStream(f3))){
            in = Nd4j.read(dis);
        }

        INDArray outAct = net.output(in);

        assertEquals(outExp, outAct);
    }

    @Test
    public void testVae() throws Exception {

        File f = new ClassPathResource("regression_testing/100a/VaeMNISTAnomaly_100a.bin").getTempFileFromArchive();
        MultiLayerNetwork net = MultiLayerNetwork.load(f, true);

        VariationalAutoencoder l0 = (VariationalAutoencoder) net.getLayer(0).conf().getLayer();
        assertEquals(new ActivationLReLU(), l0.getActivationFn());
        assertEquals(32, l0.getNOut());
        assertArrayEquals(new int[]{256, 256}, l0.getEncoderLayerSizes());
        assertArrayEquals(new int[]{256, 256}, l0.getDecoderLayerSizes());
        assertEquals(WeightInit.XAVIER, l0.getWeightInit());
        assertEquals(1e-4, l0.getL2(), 1e-6);
        assertEquals(new Adam(0.05), l0.getIUpdater());

        INDArray outExp;
        File f2 = new ClassPathResource("regression_testing/100a/VaeMNISTAnomaly_Output_100a.bin").getTempFileFromArchive();
        try(DataInputStream dis = new DataInputStream(new FileInputStream(f2))){
            outExp = Nd4j.read(dis);
        }

        INDArray in;
        File f3 = new ClassPathResource("regression_testing/100a/VaeMNISTAnomaly_Input_100a.bin").getTempFileFromArchive();
        try(DataInputStream dis = new DataInputStream(new FileInputStream(f3))){
            in = Nd4j.read(dis);
        }

        INDArray outAct = net.output(in);

        assertEquals(outExp, outAct);
    }


    @Test
    public void testYoloHouseNumber() throws Exception {

        File f = new ClassPathResource("regression_testing/100a/HouseNumberDetection_100a.bin").getTempFileFromArchive();
        ComputationGraph net = ComputationGraph.load(f, true);

        int nBoxes = 5;
        int nClasses = 10;

        ConvolutionLayer cl = (ConvolutionLayer)((LayerVertex)net.getConfiguration().getVertices().get("convolution2d_9")).getLayerConf().getLayer();
        assertEquals(nBoxes * (5 + nClasses), cl.getNOut());
        assertEquals(new ActivationIdentity(), cl.getActivationFn());
        assertEquals(ConvolutionMode.Same, cl.getConvolutionMode());
        assertEquals(WeightInit.XAVIER, cl.getWeightInit());
        assertArrayEquals(new int[]{1,1}, cl.getKernelSize());
        assertArrayEquals(new int[]{1,1}, cl.getKernelSize());

        INDArray outExp;
        File f2 = new ClassPathResource("regression_testing/100a/HouseNumberDetection_Output_100a.bin").getTempFileFromArchive();
        try(DataInputStream dis = new DataInputStream(new FileInputStream(f2))){
            outExp = Nd4j.read(dis);
        }

        INDArray in;
        File f3 = new ClassPathResource("regression_testing/100a/HouseNumberDetection_Input_100a.bin").getTempFileFromArchive();
        try(DataInputStream dis = new DataInputStream(new FileInputStream(f3))){
            in = Nd4j.read(dis);
        }

        INDArray outAct = net.outputSingle(in);

        assertEquals(outExp, outAct);
    }


}
