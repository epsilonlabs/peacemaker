/**
 */
package comicshop.impl;

import comicshop.Author;
import comicshop.Comic;
import comicshop.ComicshopPackage;
import comicshop.Publisher;
import comicshop.Shop;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Shop</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link comicshop.impl.ShopImpl#getName <em>Name</em>}</li>
 *   <li>{@link comicshop.impl.ShopImpl#getComics <em>Comics</em>}</li>
 *   <li>{@link comicshop.impl.ShopImpl#getTwoComics <em>Two Comics</em>}</li>
 *   <li>{@link comicshop.impl.ShopImpl#getThreeComics <em>Three Comics</em>}</li>
 *   <li>{@link comicshop.impl.ShopImpl#getPublishers <em>Publishers</em>}</li>
 *   <li>{@link comicshop.impl.ShopImpl#getAuthors <em>Authors</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ShopImpl extends MinimalEObjectImpl.Container implements Shop {
	/**
	 * The default value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected static final String NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected String name = NAME_EDEFAULT;

	/**
	 * The cached value of the '{@link #getComics() <em>Comics</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getComics()
	 * @generated
	 * @ordered
	 */
	protected EList<Comic> comics;

	/**
	 * The cached value of the '{@link #getTwoComics() <em>Two Comics</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTwoComics()
	 * @generated
	 * @ordered
	 */
	protected EList<Comic> twoComics;

	/**
	 * The cached value of the '{@link #getThreeComics() <em>Three Comics</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getThreeComics()
	 * @generated
	 * @ordered
	 */
	protected EList<Comic> threeComics;

	/**
	 * The cached value of the '{@link #getPublishers() <em>Publishers</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPublishers()
	 * @generated
	 * @ordered
	 */
	protected EList<Publisher> publishers;

	/**
	 * The cached value of the '{@link #getAuthors() <em>Authors</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAuthors()
	 * @generated
	 * @ordered
	 */
	protected EList<Author> authors;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ShopImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ComicshopPackage.Literals.SHOP;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setName(String newName) {
		String oldName = name;
		name = newName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ComicshopPackage.SHOP__NAME, oldName, name));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<Comic> getComics() {
		if (comics == null) {
			comics = new EObjectContainmentEList<Comic>(Comic.class, this, ComicshopPackage.SHOP__COMICS);
		}
		return comics;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<Comic> getTwoComics() {
		if (twoComics == null) {
			twoComics = new EObjectContainmentEList<Comic>(Comic.class, this, ComicshopPackage.SHOP__TWO_COMICS);
		}
		return twoComics;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<Comic> getThreeComics() {
		if (threeComics == null) {
			threeComics = new EObjectContainmentEList<Comic>(Comic.class, this, ComicshopPackage.SHOP__THREE_COMICS);
		}
		return threeComics;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<Publisher> getPublishers() {
		if (publishers == null) {
			publishers = new EObjectContainmentEList<Publisher>(Publisher.class, this, ComicshopPackage.SHOP__PUBLISHERS);
		}
		return publishers;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<Author> getAuthors() {
		if (authors == null) {
			authors = new EObjectContainmentEList<Author>(Author.class, this, ComicshopPackage.SHOP__AUTHORS);
		}
		return authors;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case ComicshopPackage.SHOP__COMICS:
				return ((InternalEList<?>)getComics()).basicRemove(otherEnd, msgs);
			case ComicshopPackage.SHOP__TWO_COMICS:
				return ((InternalEList<?>)getTwoComics()).basicRemove(otherEnd, msgs);
			case ComicshopPackage.SHOP__THREE_COMICS:
				return ((InternalEList<?>)getThreeComics()).basicRemove(otherEnd, msgs);
			case ComicshopPackage.SHOP__PUBLISHERS:
				return ((InternalEList<?>)getPublishers()).basicRemove(otherEnd, msgs);
			case ComicshopPackage.SHOP__AUTHORS:
				return ((InternalEList<?>)getAuthors()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ComicshopPackage.SHOP__NAME:
				return getName();
			case ComicshopPackage.SHOP__COMICS:
				return getComics();
			case ComicshopPackage.SHOP__TWO_COMICS:
				return getTwoComics();
			case ComicshopPackage.SHOP__THREE_COMICS:
				return getThreeComics();
			case ComicshopPackage.SHOP__PUBLISHERS:
				return getPublishers();
			case ComicshopPackage.SHOP__AUTHORS:
				return getAuthors();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case ComicshopPackage.SHOP__NAME:
				setName((String)newValue);
				return;
			case ComicshopPackage.SHOP__COMICS:
				getComics().clear();
				getComics().addAll((Collection<? extends Comic>)newValue);
				return;
			case ComicshopPackage.SHOP__TWO_COMICS:
				getTwoComics().clear();
				getTwoComics().addAll((Collection<? extends Comic>)newValue);
				return;
			case ComicshopPackage.SHOP__THREE_COMICS:
				getThreeComics().clear();
				getThreeComics().addAll((Collection<? extends Comic>)newValue);
				return;
			case ComicshopPackage.SHOP__PUBLISHERS:
				getPublishers().clear();
				getPublishers().addAll((Collection<? extends Publisher>)newValue);
				return;
			case ComicshopPackage.SHOP__AUTHORS:
				getAuthors().clear();
				getAuthors().addAll((Collection<? extends Author>)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case ComicshopPackage.SHOP__NAME:
				setName(NAME_EDEFAULT);
				return;
			case ComicshopPackage.SHOP__COMICS:
				getComics().clear();
				return;
			case ComicshopPackage.SHOP__TWO_COMICS:
				getTwoComics().clear();
				return;
			case ComicshopPackage.SHOP__THREE_COMICS:
				getThreeComics().clear();
				return;
			case ComicshopPackage.SHOP__PUBLISHERS:
				getPublishers().clear();
				return;
			case ComicshopPackage.SHOP__AUTHORS:
				getAuthors().clear();
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case ComicshopPackage.SHOP__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case ComicshopPackage.SHOP__COMICS:
				return comics != null && !comics.isEmpty();
			case ComicshopPackage.SHOP__TWO_COMICS:
				return twoComics != null && !twoComics.isEmpty();
			case ComicshopPackage.SHOP__THREE_COMICS:
				return threeComics != null && !threeComics.isEmpty();
			case ComicshopPackage.SHOP__PUBLISHERS:
				return publishers != null && !publishers.isEmpty();
			case ComicshopPackage.SHOP__AUTHORS:
				return authors != null && !authors.isEmpty();
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuilder result = new StringBuilder(super.toString());
		result.append(" (name: ");
		result.append(name);
		result.append(')');
		return result.toString();
	}

} //ShopImpl
