package org.amshove.natls.explore;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ExploreApp extends Application
{
	@Override
	public void start(Stage stage) throws IOException
	{
		FXMLLoader fxmlLoader = new FXMLLoader(ExploreApp.class.getResource("explore.fxml"));
		Scene scene = new Scene(fxmlLoader.load(), 1024, 768);
		stage.setTitle("NatLS Explore");
		stage.setScene(scene);
		stage.show();
	}

	public static void main(String[] args)
	{
		launch();
	}
}
