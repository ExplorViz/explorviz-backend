package net.explorviz.api;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.explorviz.model.landscape.Landscape;
import net.explorviz.model.store.Timestamp;
import net.explorviz.model.store.helper.TimestampHelper;
import net.explorviz.repository.LandscapeExchangeService;
import net.explorviz.repository.RepositoryStorage;
import net.explorviz.server.main.Configuration;
import net.explorviz.server.providers.CoreModelHandler;
import net.explorviz.server.providers.GenericTypeFinder;
import net.explorviz.shared.server.helper.PropertyService;

/**
 * Interface implementation of the extension API - Offers version information
 * and provides landscapes and timestamps
 *
 * @author Christian Zirkelbach (czi@informatik.uni-kiel.de)
 *
 */
@Service
@Singleton
public final class ExtensionAPIImpl implements IExtensionAPI {

	static final Logger LOGGER = LoggerFactory.getLogger(ExtensionAPIImpl.class.getName());

	String versionNumber = "1.2.0a";
	LandscapeExchangeService service;

	@Inject
	public ExtensionAPIImpl(final LandscapeExchangeService service) {
		this.service = service;
	}

	/**
	 * Reveals the currently provides API version
	 */
	@Override
	public String getAPIVersion() {
		return "ExplorViz Extension API version: " + versionNumber;
	}

	/**
	 * Provides the current landscape
	 */
	@Override
	public Landscape getLatestLandscape() {
		return service.getCurrentLandscape();
	}

	/**
	 * Provides a specific landscape determined by a passed timestamp
	 *
	 * @param {@link Timestamp} (as configured in Kieker)
	 */
	@Override
	public Landscape getLandscape(final long timestamp, final String folderName) {
		try {
			return service.getLandscape(timestamp, folderName);
		} catch (final FileNotFoundException e) {
			LOGGER.debug("Specific landscape not found!", e.getMessage());
			throw new NoSuchElementException("The requested landscape could not be found");
		}
	}

	/**
	 * Provides the "intervalSize" newest timestamps within the server
	 *
	 * @param intervalSize (number of retrieved timestamps)
	 * @return List of {@link Timestamp}
	 */
	@Override
	public List<Timestamp> getNewestTimestamps(final int intervalSize) {
		final List<Timestamp> allTimestamps = this.service
				.getTimestampObjectsInRepo(Configuration.LANDSCAPE_REPOSITORY);
		return TimestampHelper.filterMostRecentTimestamps(allTimestamps, intervalSize);
	}

	/**
	 * Provides the "intervalSize" oldest timestamps within the server
	 *
	 * @param intervalSize (number of retrieved timestamps)
	 * @return List of Timestamp
	 */

	@Override
	public List<Timestamp> getOldestTimestamps(final int intervalSize) {
		final List<Timestamp> allTimestamps = this.service
				.getTimestampObjectsInRepo(Configuration.LANDSCAPE_REPOSITORY);
		return TimestampHelper.filterOldestTimestamps(allTimestamps, intervalSize);
	}

	/**
	 * Provides the "intervalSize" timestamps before a passed "fromTimestamp" within
	 * the server
	 *
	 * @param fromTimestamp (timestamp, which sets the limit)
	 * @param intervalSize  (number of retrieved timestamps)
	 * @return List of Timestamp
	 */
	@Override
	public List<Timestamp> getPreviousTimestamps(final long fromTimestamp, final int intervalSize) {
		final List<Timestamp> allTimestamps = this.service
				.getTimestampObjectsInRepo(Configuration.LANDSCAPE_REPOSITORY);
		return TimestampHelper.filterTimestampsBeforeTimestamp(allTimestamps, fromTimestamp, intervalSize);
	}

	/**
	 * Provides the "intervalSize" timestamps after a passed "afterTimestamp" within
	 * the server
	 *
	 * @param afterTimestamp (timestamp, which sets the limit)
	 * @param intervalSize   (number of retrieved timestamps)
	 * @return List of Timestamp
	 */
	@Override
	public List<Timestamp> getSubsequentTimestamps(final long afterTimestamp, final int intervalSize) {
		final List<Timestamp> allTimestamps = this.service
				.getTimestampObjectsInRepo(Configuration.LANDSCAPE_REPOSITORY);
		return TimestampHelper.filterTimestampsAfterTimestamp(allTimestamps, afterTimestamp, intervalSize);
	}

	public List<Timestamp> getUploadedTimestamps() {
		final List<Timestamp> allTimestamps = this.service.getTimestampObjectsInRepo(Configuration.REPLAY_REPOSITORY);
		return allTimestamps;
	}

	/**
	 * Registers specific core model types for the JSONAPI provider (necessary by
	 * extensions, if they want to use specific backend models) - e.g.,
	 * ("Timestamp", Timestamp.class)
	 */
	@Override
	public void registerSpecificCoreModels(final Map<String, Class<?>> typeMap) {
		for (final Map.Entry<String, Class<?>> entry : typeMap.entrySet()) {
			GenericTypeFinder.typeMap.putIfAbsent(entry.getKey(), entry.getValue());
		}

	}

	/**
	 * Registers specific model types for the JSONAPI provider (necessary by
	 * extensions, if they want to use specific or custom backend models) - e.g.,
	 * ("Timestamp", Timestamp.class)
	 */
	@Override
	public void registerSpecificModel(final String typeName, final Class<?> type) {
		GenericTypeFinder.typeMap.putIfAbsent(typeName, type);

	}

	/**
	 * Registers all core model types for the JSONAPI provider (necessary by
	 * extensions, if they want to use all backend models)
	 */
	@Override
	public void registerAllCoreModels() {
		CoreModelHandler.registerAllCoreModels();

	}

	/**
	 * Enable / disable dummy mode true = Use dummy monitoring data instead of real
	 * monitoring data
	 *
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	@Override
	public void setDummyMode(final boolean value) throws FileNotFoundException, IOException {
		PropertyService.setBooleanProperty("useDummyMode", value);
	}

	@Override
	public void saveLandscapeToFile(final Landscape landscape, final String folderName) {
		RepositoryStorage.writeToFile(landscape, landscape.getTimestamp().getTimestamp(), folderName);
	}

}
