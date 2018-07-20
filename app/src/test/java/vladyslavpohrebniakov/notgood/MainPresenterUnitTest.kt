package vladyslavpohrebniakov.notgood

import com.nhaarman.mockito_kotlin.*
import org.junit.Assert
import org.junit.Test
import vladyslavpohrebniakov.notgood.features.main.MainPresenter
import vladyslavpohrebniakov.notgood.features.main.MainView
import vladyslavpohrebniakov.notgood.receiver.ReceiverActions

class MainPresenterUnitTest {
	@Test
	fun `should show progress`() {
		val mockview: MainView = mock()
		val objectUnderTest = MainPresenter(mockview)

		objectUnderTest.showProgress()

		verify(mockview).setProgressGroupVisibility(true)
		verify(mockview).hideLastUpdateCard()
		verify(mockview).setAlbumInfoGroupVisibility(false)
	}

	@Test
	fun `should hide progress`() {
		val mockview: MainView = mock()
		val objectUnderTest = MainPresenter(mockview)

		objectUnderTest.hideProgress()

		verify(mockview).setProgressGroupVisibility(false)
		verify(mockview).setAlbumInfoGroupVisibility(true)
		verify(mockview).setLastUpdateText(any())
	}

	@Test
	fun `should show progress text`() {
		val progressTestText = "progress test text"
		val mockview: MainView = mock()
		val objectUnderTest = MainPresenter(mockview)

		objectUnderTest.showProgressText(progressTestText)

		verify(mockview).setProgressText(progressTestText)
	}

	@Test
	fun `should show reviews added text`() {
		val testCount = 10
		val mockview: MainView = mock()
		val objectUnderTest = MainPresenter(mockview)

		objectUnderTest.showReviewsAddedText(testCount)

		verify(mockview).setReviewsAddedText(testCount)
	}

	@Test
	fun `should show rating`() {
		val testArtist = "artist"
		val testAlbum = "album"
		val testRating = "10/10"
		val mockview: MainView = mock()
		val objectUnderTest = MainPresenter(mockview)

		objectUnderTest.showRating(testArtist, testAlbum, testRating)

		verify(mockview).setRatingText(testArtist, testAlbum, testRating)
	}

	@Test
	fun `should show rating from notification`() {
		val testArtist = "artist"
		val testAlbum = "album"
		val testRating = "10/10"
		val testExtras = arrayOfNulls<String>(3)
		testExtras[0] = testArtist
		testExtras[1] = testAlbum
		testExtras[2] = testRating
		val mockview: MainView = mock()
		val objectUnderTest = MainPresenter(mockview)

		whenever(mockview.getExtrasFromIntent()).doReturn(testExtras)
		objectUnderTest.showRating(testArtist, testAlbum, testRating)

		verify(mockview).setRatingText(testArtist, testAlbum, testRating)
	}

	@Test
	fun `should show last update date`() {
		val mockview: MainView = mock()
		val objectUnderTest = MainPresenter(mockview)

		objectUnderTest.showLastUpdateDate()

		verify(mockview).setLastUpdateText(any())
	}

	@Test
	fun `should set album art`() {
		val link = "https://link"
		val mockview: MainView = mock()
		val objectUnderTest = MainPresenter(mockview)

		objectUnderTest.showAlbumArt(link)

		verify(mockview).setAlbumArt(link)
	}

	@Test
	fun `should return null on load album art link from notification`() {
		val testExtras = arrayOfNulls<String>(3)
		val mockview: MainView = mock()
		val objectUnderTest = MainPresenter(mockview)

		whenever(mockview.getExtrasFromIntent()).doReturn(testExtras)

		Assert.assertEquals(objectUnderTest.loadAlbumArtLinkFromNotification(), null)
	}

	@Test
	fun `should show about dialog`() {
		val mockview: MainView = mock()
		val objectUnderTest = MainPresenter(mockview)

		objectUnderTest.showAboutDialog()

		verify(mockview).showAboutAppDialog()
	}

	@Test
	fun `should show licenses dialog`() {
		val mockview: MainView = mock()
		val objectUnderTest = MainPresenter(mockview)

		objectUnderTest.showLicensesDialog()

		verify(mockview).showOpenSourceLicensesDialog()
	}

	@Test
	fun `should register receiver`() {
		val mockview: MainView = mock()
		val objectUnderTest = MainPresenter(mockview)

		objectUnderTest.registerBroadcastReceiver()

		verify(mockview).registerBroadcastReceiver(ReceiverActions.BROADCAST_RECEIVER_ACTIONS)
	}

	@Test
	fun `should unregister receiver`() {
		val mockview: MainView = mock()
		val objectUnderTest = MainPresenter(mockview)

		objectUnderTest.unregisterBroadcastReceiver()

		verify(mockview).unregisterBroadcastReceiver()
	}

	@Test
	fun `should start service`() {
		val mockview: MainView = mock()
		val objectUnderTest = MainPresenter(mockview)

		whenever(mockview.getAllowForegroundService()).doReturn(true)
		objectUnderTest.startService()

		verify(mockview).setAllowSwitchChecked(true)
		verify(mockview).startService()
	}

	@Test
	fun `should not start service`() {
		val mockview: MainView = mock()
		val objectUnderTest = MainPresenter(mockview)

		whenever(mockview.getAllowForegroundService()).doReturn(false)
		objectUnderTest.startService()

		verify(mockview).setAllowSwitchChecked(false)
	}

	@Test
	fun `should stop service`() {
		val mockview: MainView = mock()
		val objectUnderTest = MainPresenter(mockview)

		objectUnderTest.allowForegroundService(false)

		verify(mockview).stopService()
	}

	@Test
	fun `should allow and start service`() {
		val mockview: MainView = mock()
		val objectUnderTest = MainPresenter(mockview)

		objectUnderTest.allowForegroundService(true)

		verify(mockview).startService()
	}

	@Test
	fun `should show or hide search card`() {
		val mockview: MainView = mock()
		val objectUnerTest = MainPresenter(mockview)

		objectUnerTest.setSearchCardVisibility(any())

		verify(mockview).setSearchCardVisibilty(any())
	}

	@Test
	fun `should search rating`() {
		val mockview: MainView = mock()
		val objectUnderTest = MainPresenter(mockview)

		objectUnderTest.searchRatingManually("text", "text")

		verify(mockview).searchRating(true, "text", "text")
	}

	@Test
	fun `should not search rating`() {
		val mockview: MainView = mock()
		val objectUnderTest = MainPresenter(mockview)

		objectUnderTest.searchRatingManually("", "")

		verify(mockview).searchRating(false, "", "")
	}
}