/**
 */
package comicshop;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Shop</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link comicshop.Shop#getName <em>Name</em>}</li>
 *   <li>{@link comicshop.Shop#getComics <em>Comics</em>}</li>
 *   <li>{@link comicshop.Shop#getPublishers <em>Publishers</em>}</li>
 *   <li>{@link comicshop.Shop#getAuthors <em>Authors</em>}</li>
 * </ul>
 *
 * @see comicshop.ComicshopPackage#getShop()
 * @model
 * @generated
 */
public interface Shop extends EObject {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see comicshop.ComicshopPackage#getShop_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link comicshop.Shop#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Comics</b></em>' containment reference list.
	 * The list contents are of type {@link comicshop.Comic}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Comics</em>' containment reference list.
	 * @see comicshop.ComicshopPackage#getShop_Comics()
	 * @model containment="true"
	 * @generated
	 */
	EList<Comic> getComics();

	/**
	 * Returns the value of the '<em><b>Publishers</b></em>' containment reference list.
	 * The list contents are of type {@link comicshop.Publisher}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Publishers</em>' containment reference list.
	 * @see comicshop.ComicshopPackage#getShop_Publishers()
	 * @model containment="true"
	 * @generated
	 */
	EList<Publisher> getPublishers();

	/**
	 * Returns the value of the '<em><b>Authors</b></em>' containment reference list.
	 * The list contents are of type {@link comicshop.Author}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Authors</em>' containment reference list.
	 * @see comicshop.ComicshopPackage#getShop_Authors()
	 * @model containment="true"
	 * @generated
	 */
	EList<Author> getAuthors();

} // Shop
