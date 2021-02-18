/**
 */
package comicshop;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Comic</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link comicshop.Comic#getTitle <em>Title</em>}</li>
 *   <li>{@link comicshop.Comic#getReleaseDate <em>Release Date</em>}</li>
 *   <li>{@link comicshop.Comic#getAuthors <em>Authors</em>}</li>
 *   <li>{@link comicshop.Comic#getPrologue <em>Prologue</em>}</li>
 *   <li>{@link comicshop.Comic#getPublisher <em>Publisher</em>}</li>
 *   <li>{@link comicshop.Comic#getReviews <em>Reviews</em>}</li>
 * </ul>
 *
 * @see comicshop.ComicshopPackage#getComic()
 * @model
 * @generated
 */
public interface Comic extends EObject {
	/**
	 * Returns the value of the '<em><b>Title</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Title</em>' attribute.
	 * @see #setTitle(String)
	 * @see comicshop.ComicshopPackage#getComic_Title()
	 * @model
	 * @generated
	 */
	String getTitle();

	/**
	 * Sets the value of the '{@link comicshop.Comic#getTitle <em>Title</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Title</em>' attribute.
	 * @see #getTitle()
	 * @generated
	 */
	void setTitle(String value);

	/**
	 * Returns the value of the '<em><b>Release Date</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Release Date</em>' attribute.
	 * @see #setReleaseDate(String)
	 * @see comicshop.ComicshopPackage#getComic_ReleaseDate()
	 * @model
	 * @generated
	 */
	String getReleaseDate();

	/**
	 * Sets the value of the '{@link comicshop.Comic#getReleaseDate <em>Release Date</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Release Date</em>' attribute.
	 * @see #getReleaseDate()
	 * @generated
	 */
	void setReleaseDate(String value);

	/**
	 * Returns the value of the '<em><b>Authors</b></em>' reference list.
	 * The list contents are of type {@link comicshop.Author}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Authors</em>' reference list.
	 * @see comicshop.ComicshopPackage#getComic_Authors()
	 * @model
	 * @generated
	 */
	EList<Author> getAuthors();

	/**
	 * Returns the value of the '<em><b>Prologue</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Prologue</em>' containment reference.
	 * @see #setPrologue(Prologue)
	 * @see comicshop.ComicshopPackage#getComic_Prologue()
	 * @model containment="true"
	 * @generated
	 */
	Prologue getPrologue();

	/**
	 * Sets the value of the '{@link comicshop.Comic#getPrologue <em>Prologue</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Prologue</em>' containment reference.
	 * @see #getPrologue()
	 * @generated
	 */
	void setPrologue(Prologue value);

	/**
	 * Returns the value of the '<em><b>Publisher</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Publisher</em>' reference.
	 * @see #setPublisher(Publisher)
	 * @see comicshop.ComicshopPackage#getComic_Publisher()
	 * @model
	 * @generated
	 */
	Publisher getPublisher();

	/**
	 * Sets the value of the '{@link comicshop.Comic#getPublisher <em>Publisher</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Publisher</em>' reference.
	 * @see #getPublisher()
	 * @generated
	 */
	void setPublisher(Publisher value);

	/**
	 * Returns the value of the '<em><b>Reviews</b></em>' containment reference list.
	 * The list contents are of type {@link comicshop.Review}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Reviews</em>' containment reference list.
	 * @see comicshop.ComicshopPackage#getComic_Reviews()
	 * @model containment="true"
	 * @generated
	 */
	EList<Review> getReviews();

} // Comic
