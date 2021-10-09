/**
 */
package comicshop;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each operation of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see comicshop.ComicshopFactory
 * @model kind="package"
 * @generated
 */
public interface ComicshopPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "comicshop";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "comicshop";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "comicshop";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	ComicshopPackage eINSTANCE = comicshop.impl.ComicshopPackageImpl.init();

	/**
	 * The meta object id for the '{@link comicshop.impl.ShopImpl <em>Shop</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see comicshop.impl.ShopImpl
	 * @see comicshop.impl.ComicshopPackageImpl#getShop()
	 * @generated
	 */
	int SHOP = 0;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SHOP__NAME = 0;

	/**
	 * The feature id for the '<em><b>Comics</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SHOP__COMICS = 1;

	/**
	 * The feature id for the '<em><b>Two Comics</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SHOP__TWO_COMICS = 2;

	/**
	 * The feature id for the '<em><b>Three Comics</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SHOP__THREE_COMICS = 3;

	/**
	 * The feature id for the '<em><b>Publishers</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SHOP__PUBLISHERS = 4;

	/**
	 * The feature id for the '<em><b>Authors</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SHOP__AUTHORS = 5;

	/**
	 * The number of structural features of the '<em>Shop</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SHOP_FEATURE_COUNT = 6;

	/**
	 * The number of operations of the '<em>Shop</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SHOP_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link comicshop.impl.ComicImpl <em>Comic</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see comicshop.impl.ComicImpl
	 * @see comicshop.impl.ComicshopPackageImpl#getComic()
	 * @generated
	 */
	int COMIC = 1;

	/**
	 * The feature id for the '<em><b>Title</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMIC__TITLE = 0;

	/**
	 * The feature id for the '<em><b>Release Date</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMIC__RELEASE_DATE = 1;

	/**
	 * The feature id for the '<em><b>Authors</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMIC__AUTHORS = 2;

	/**
	 * The feature id for the '<em><b>Prologue</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMIC__PROLOGUE = 3;

	/**
	 * The feature id for the '<em><b>Publisher</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMIC__PUBLISHER = 4;

	/**
	 * The feature id for the '<em><b>Reviews</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMIC__REVIEWS = 5;

	/**
	 * The number of structural features of the '<em>Comic</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMIC_FEATURE_COUNT = 6;

	/**
	 * The number of operations of the '<em>Comic</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMIC_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link comicshop.impl.ReviewImpl <em>Review</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see comicshop.impl.ReviewImpl
	 * @see comicshop.impl.ComicshopPackageImpl#getReview()
	 * @generated
	 */
	int REVIEW = 2;

	/**
	 * The feature id for the '<em><b>Author</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REVIEW__AUTHOR = 0;

	/**
	 * The feature id for the '<em><b>Date</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REVIEW__DATE = 1;

	/**
	 * The feature id for the '<em><b>Title</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REVIEW__TITLE = 2;

	/**
	 * The feature id for the '<em><b>Text</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REVIEW__TEXT = 3;

	/**
	 * The feature id for the '<em><b>Stars</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REVIEW__STARS = 4;

	/**
	 * The number of structural features of the '<em>Review</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REVIEW_FEATURE_COUNT = 5;

	/**
	 * The number of operations of the '<em>Review</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REVIEW_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link comicshop.impl.PrologueImpl <em>Prologue</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see comicshop.impl.PrologueImpl
	 * @see comicshop.impl.ComicshopPackageImpl#getPrologue()
	 * @generated
	 */
	int PROLOGUE = 3;

	/**
	 * The feature id for the '<em><b>Author</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROLOGUE__AUTHOR = 0;

	/**
	 * The feature id for the '<em><b>Text</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROLOGUE__TEXT = 1;

	/**
	 * The number of structural features of the '<em>Prologue</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROLOGUE_FEATURE_COUNT = 2;

	/**
	 * The number of operations of the '<em>Prologue</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROLOGUE_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link comicshop.impl.PublisherImpl <em>Publisher</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see comicshop.impl.PublisherImpl
	 * @see comicshop.impl.ComicshopPackageImpl#getPublisher()
	 * @generated
	 */
	int PUBLISHER = 4;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PUBLISHER__NAME = 0;

	/**
	 * The number of structural features of the '<em>Publisher</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PUBLISHER_FEATURE_COUNT = 1;

	/**
	 * The number of operations of the '<em>Publisher</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PUBLISHER_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link comicshop.impl.AuthorImpl <em>Author</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see comicshop.impl.AuthorImpl
	 * @see comicshop.impl.ComicshopPackageImpl#getAuthor()
	 * @generated
	 */
	int AUTHOR = 5;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int AUTHOR__NAME = 0;

	/**
	 * The number of structural features of the '<em>Author</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int AUTHOR_FEATURE_COUNT = 1;

	/**
	 * The number of operations of the '<em>Author</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int AUTHOR_OPERATION_COUNT = 0;


	/**
	 * Returns the meta object for class '{@link comicshop.Shop <em>Shop</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Shop</em>'.
	 * @see comicshop.Shop
	 * @generated
	 */
	EClass getShop();

	/**
	 * Returns the meta object for the attribute '{@link comicshop.Shop#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see comicshop.Shop#getName()
	 * @see #getShop()
	 * @generated
	 */
	EAttribute getShop_Name();

	/**
	 * Returns the meta object for the containment reference list '{@link comicshop.Shop#getComics <em>Comics</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Comics</em>'.
	 * @see comicshop.Shop#getComics()
	 * @see #getShop()
	 * @generated
	 */
	EReference getShop_Comics();

	/**
	 * Returns the meta object for the containment reference list '{@link comicshop.Shop#getTwoComics <em>Two Comics</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Two Comics</em>'.
	 * @see comicshop.Shop#getTwoComics()
	 * @see #getShop()
	 * @generated
	 */
	EReference getShop_TwoComics();

	/**
	 * Returns the meta object for the containment reference list '{@link comicshop.Shop#getThreeComics <em>Three Comics</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Three Comics</em>'.
	 * @see comicshop.Shop#getThreeComics()
	 * @see #getShop()
	 * @generated
	 */
	EReference getShop_ThreeComics();

	/**
	 * Returns the meta object for the containment reference list '{@link comicshop.Shop#getPublishers <em>Publishers</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Publishers</em>'.
	 * @see comicshop.Shop#getPublishers()
	 * @see #getShop()
	 * @generated
	 */
	EReference getShop_Publishers();

	/**
	 * Returns the meta object for the containment reference list '{@link comicshop.Shop#getAuthors <em>Authors</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Authors</em>'.
	 * @see comicshop.Shop#getAuthors()
	 * @see #getShop()
	 * @generated
	 */
	EReference getShop_Authors();

	/**
	 * Returns the meta object for class '{@link comicshop.Comic <em>Comic</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Comic</em>'.
	 * @see comicshop.Comic
	 * @generated
	 */
	EClass getComic();

	/**
	 * Returns the meta object for the attribute '{@link comicshop.Comic#getTitle <em>Title</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Title</em>'.
	 * @see comicshop.Comic#getTitle()
	 * @see #getComic()
	 * @generated
	 */
	EAttribute getComic_Title();

	/**
	 * Returns the meta object for the attribute '{@link comicshop.Comic#getReleaseDate <em>Release Date</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Release Date</em>'.
	 * @see comicshop.Comic#getReleaseDate()
	 * @see #getComic()
	 * @generated
	 */
	EAttribute getComic_ReleaseDate();

	/**
	 * Returns the meta object for the reference list '{@link comicshop.Comic#getAuthors <em>Authors</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Authors</em>'.
	 * @see comicshop.Comic#getAuthors()
	 * @see #getComic()
	 * @generated
	 */
	EReference getComic_Authors();

	/**
	 * Returns the meta object for the containment reference '{@link comicshop.Comic#getPrologue <em>Prologue</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Prologue</em>'.
	 * @see comicshop.Comic#getPrologue()
	 * @see #getComic()
	 * @generated
	 */
	EReference getComic_Prologue();

	/**
	 * Returns the meta object for the reference '{@link comicshop.Comic#getPublisher <em>Publisher</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Publisher</em>'.
	 * @see comicshop.Comic#getPublisher()
	 * @see #getComic()
	 * @generated
	 */
	EReference getComic_Publisher();

	/**
	 * Returns the meta object for the containment reference list '{@link comicshop.Comic#getReviews <em>Reviews</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Reviews</em>'.
	 * @see comicshop.Comic#getReviews()
	 * @see #getComic()
	 * @generated
	 */
	EReference getComic_Reviews();

	/**
	 * Returns the meta object for class '{@link comicshop.Review <em>Review</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Review</em>'.
	 * @see comicshop.Review
	 * @generated
	 */
	EClass getReview();

	/**
	 * Returns the meta object for the attribute '{@link comicshop.Review#getAuthor <em>Author</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Author</em>'.
	 * @see comicshop.Review#getAuthor()
	 * @see #getReview()
	 * @generated
	 */
	EAttribute getReview_Author();

	/**
	 * Returns the meta object for the attribute '{@link comicshop.Review#getDate <em>Date</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Date</em>'.
	 * @see comicshop.Review#getDate()
	 * @see #getReview()
	 * @generated
	 */
	EAttribute getReview_Date();

	/**
	 * Returns the meta object for the attribute '{@link comicshop.Review#getTitle <em>Title</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Title</em>'.
	 * @see comicshop.Review#getTitle()
	 * @see #getReview()
	 * @generated
	 */
	EAttribute getReview_Title();

	/**
	 * Returns the meta object for the attribute '{@link comicshop.Review#getText <em>Text</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Text</em>'.
	 * @see comicshop.Review#getText()
	 * @see #getReview()
	 * @generated
	 */
	EAttribute getReview_Text();

	/**
	 * Returns the meta object for the attribute '{@link comicshop.Review#getStars <em>Stars</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Stars</em>'.
	 * @see comicshop.Review#getStars()
	 * @see #getReview()
	 * @generated
	 */
	EAttribute getReview_Stars();

	/**
	 * Returns the meta object for class '{@link comicshop.Prologue <em>Prologue</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Prologue</em>'.
	 * @see comicshop.Prologue
	 * @generated
	 */
	EClass getPrologue();

	/**
	 * Returns the meta object for the attribute '{@link comicshop.Prologue#getAuthor <em>Author</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Author</em>'.
	 * @see comicshop.Prologue#getAuthor()
	 * @see #getPrologue()
	 * @generated
	 */
	EAttribute getPrologue_Author();

	/**
	 * Returns the meta object for the attribute '{@link comicshop.Prologue#getText <em>Text</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Text</em>'.
	 * @see comicshop.Prologue#getText()
	 * @see #getPrologue()
	 * @generated
	 */
	EAttribute getPrologue_Text();

	/**
	 * Returns the meta object for class '{@link comicshop.Publisher <em>Publisher</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Publisher</em>'.
	 * @see comicshop.Publisher
	 * @generated
	 */
	EClass getPublisher();

	/**
	 * Returns the meta object for the attribute '{@link comicshop.Publisher#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see comicshop.Publisher#getName()
	 * @see #getPublisher()
	 * @generated
	 */
	EAttribute getPublisher_Name();

	/**
	 * Returns the meta object for class '{@link comicshop.Author <em>Author</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Author</em>'.
	 * @see comicshop.Author
	 * @generated
	 */
	EClass getAuthor();

	/**
	 * Returns the meta object for the attribute '{@link comicshop.Author#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see comicshop.Author#getName()
	 * @see #getAuthor()
	 * @generated
	 */
	EAttribute getAuthor_Name();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	ComicshopFactory getComicshopFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each operation of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link comicshop.impl.ShopImpl <em>Shop</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see comicshop.impl.ShopImpl
		 * @see comicshop.impl.ComicshopPackageImpl#getShop()
		 * @generated
		 */
		EClass SHOP = eINSTANCE.getShop();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SHOP__NAME = eINSTANCE.getShop_Name();

		/**
		 * The meta object literal for the '<em><b>Comics</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference SHOP__COMICS = eINSTANCE.getShop_Comics();

		/**
		 * The meta object literal for the '<em><b>Two Comics</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference SHOP__TWO_COMICS = eINSTANCE.getShop_TwoComics();

		/**
		 * The meta object literal for the '<em><b>Three Comics</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference SHOP__THREE_COMICS = eINSTANCE.getShop_ThreeComics();

		/**
		 * The meta object literal for the '<em><b>Publishers</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference SHOP__PUBLISHERS = eINSTANCE.getShop_Publishers();

		/**
		 * The meta object literal for the '<em><b>Authors</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference SHOP__AUTHORS = eINSTANCE.getShop_Authors();

		/**
		 * The meta object literal for the '{@link comicshop.impl.ComicImpl <em>Comic</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see comicshop.impl.ComicImpl
		 * @see comicshop.impl.ComicshopPackageImpl#getComic()
		 * @generated
		 */
		EClass COMIC = eINSTANCE.getComic();

		/**
		 * The meta object literal for the '<em><b>Title</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute COMIC__TITLE = eINSTANCE.getComic_Title();

		/**
		 * The meta object literal for the '<em><b>Release Date</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute COMIC__RELEASE_DATE = eINSTANCE.getComic_ReleaseDate();

		/**
		 * The meta object literal for the '<em><b>Authors</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference COMIC__AUTHORS = eINSTANCE.getComic_Authors();

		/**
		 * The meta object literal for the '<em><b>Prologue</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference COMIC__PROLOGUE = eINSTANCE.getComic_Prologue();

		/**
		 * The meta object literal for the '<em><b>Publisher</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference COMIC__PUBLISHER = eINSTANCE.getComic_Publisher();

		/**
		 * The meta object literal for the '<em><b>Reviews</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference COMIC__REVIEWS = eINSTANCE.getComic_Reviews();

		/**
		 * The meta object literal for the '{@link comicshop.impl.ReviewImpl <em>Review</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see comicshop.impl.ReviewImpl
		 * @see comicshop.impl.ComicshopPackageImpl#getReview()
		 * @generated
		 */
		EClass REVIEW = eINSTANCE.getReview();

		/**
		 * The meta object literal for the '<em><b>Author</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REVIEW__AUTHOR = eINSTANCE.getReview_Author();

		/**
		 * The meta object literal for the '<em><b>Date</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REVIEW__DATE = eINSTANCE.getReview_Date();

		/**
		 * The meta object literal for the '<em><b>Title</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REVIEW__TITLE = eINSTANCE.getReview_Title();

		/**
		 * The meta object literal for the '<em><b>Text</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REVIEW__TEXT = eINSTANCE.getReview_Text();

		/**
		 * The meta object literal for the '<em><b>Stars</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REVIEW__STARS = eINSTANCE.getReview_Stars();

		/**
		 * The meta object literal for the '{@link comicshop.impl.PrologueImpl <em>Prologue</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see comicshop.impl.PrologueImpl
		 * @see comicshop.impl.ComicshopPackageImpl#getPrologue()
		 * @generated
		 */
		EClass PROLOGUE = eINSTANCE.getPrologue();

		/**
		 * The meta object literal for the '<em><b>Author</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PROLOGUE__AUTHOR = eINSTANCE.getPrologue_Author();

		/**
		 * The meta object literal for the '<em><b>Text</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PROLOGUE__TEXT = eINSTANCE.getPrologue_Text();

		/**
		 * The meta object literal for the '{@link comicshop.impl.PublisherImpl <em>Publisher</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see comicshop.impl.PublisherImpl
		 * @see comicshop.impl.ComicshopPackageImpl#getPublisher()
		 * @generated
		 */
		EClass PUBLISHER = eINSTANCE.getPublisher();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PUBLISHER__NAME = eINSTANCE.getPublisher_Name();

		/**
		 * The meta object literal for the '{@link comicshop.impl.AuthorImpl <em>Author</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see comicshop.impl.AuthorImpl
		 * @see comicshop.impl.ComicshopPackageImpl#getAuthor()
		 * @generated
		 */
		EClass AUTHOR = eINSTANCE.getAuthor();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute AUTHOR__NAME = eINSTANCE.getAuthor_Name();

	}

} //ComicshopPackage
