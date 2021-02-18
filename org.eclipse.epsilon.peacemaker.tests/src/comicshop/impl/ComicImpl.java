/**
 */
package comicshop.impl;

import comicshop.Author;
import comicshop.Comic;
import comicshop.ComicshopPackage;
import comicshop.Prologue;
import comicshop.Publisher;
import comicshop.Review;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.EObjectResolvingEList;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Comic</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link comicshop.impl.ComicImpl#getTitle <em>Title</em>}</li>
 *   <li>{@link comicshop.impl.ComicImpl#getReleaseDate <em>Release Date</em>}</li>
 *   <li>{@link comicshop.impl.ComicImpl#getAuthors <em>Authors</em>}</li>
 *   <li>{@link comicshop.impl.ComicImpl#getPrologue <em>Prologue</em>}</li>
 *   <li>{@link comicshop.impl.ComicImpl#getPublisher <em>Publisher</em>}</li>
 *   <li>{@link comicshop.impl.ComicImpl#getReviews <em>Reviews</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ComicImpl extends MinimalEObjectImpl.Container implements Comic {
	/**
	 * The default value of the '{@link #getTitle() <em>Title</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTitle()
	 * @generated
	 * @ordered
	 */
	protected static final String TITLE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getTitle() <em>Title</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTitle()
	 * @generated
	 * @ordered
	 */
	protected String title = TITLE_EDEFAULT;

	/**
	 * The default value of the '{@link #getReleaseDate() <em>Release Date</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getReleaseDate()
	 * @generated
	 * @ordered
	 */
	protected static final String RELEASE_DATE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getReleaseDate() <em>Release Date</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getReleaseDate()
	 * @generated
	 * @ordered
	 */
	protected String releaseDate = RELEASE_DATE_EDEFAULT;

	/**
	 * The cached value of the '{@link #getAuthors() <em>Authors</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAuthors()
	 * @generated
	 * @ordered
	 */
	protected EList<Author> authors;

	/**
	 * The cached value of the '{@link #getPrologue() <em>Prologue</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPrologue()
	 * @generated
	 * @ordered
	 */
	protected Prologue prologue;

	/**
	 * The cached value of the '{@link #getPublisher() <em>Publisher</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPublisher()
	 * @generated
	 * @ordered
	 */
	protected Publisher publisher;

	/**
	 * The cached value of the '{@link #getReviews() <em>Reviews</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getReviews()
	 * @generated
	 * @ordered
	 */
	protected EList<Review> reviews;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ComicImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ComicshopPackage.Literals.COMIC;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getTitle() {
		return title;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setTitle(String newTitle) {
		String oldTitle = title;
		title = newTitle;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ComicshopPackage.COMIC__TITLE, oldTitle, title));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getReleaseDate() {
		return releaseDate;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setReleaseDate(String newReleaseDate) {
		String oldReleaseDate = releaseDate;
		releaseDate = newReleaseDate;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ComicshopPackage.COMIC__RELEASE_DATE, oldReleaseDate, releaseDate));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<Author> getAuthors() {
		if (authors == null) {
			authors = new EObjectResolvingEList<Author>(Author.class, this, ComicshopPackage.COMIC__AUTHORS);
		}
		return authors;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Prologue getPrologue() {
		return prologue;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetPrologue(Prologue newPrologue, NotificationChain msgs) {
		Prologue oldPrologue = prologue;
		prologue = newPrologue;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ComicshopPackage.COMIC__PROLOGUE, oldPrologue, newPrologue);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setPrologue(Prologue newPrologue) {
		if (newPrologue != prologue) {
			NotificationChain msgs = null;
			if (prologue != null)
				msgs = ((InternalEObject)prologue).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ComicshopPackage.COMIC__PROLOGUE, null, msgs);
			if (newPrologue != null)
				msgs = ((InternalEObject)newPrologue).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ComicshopPackage.COMIC__PROLOGUE, null, msgs);
			msgs = basicSetPrologue(newPrologue, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ComicshopPackage.COMIC__PROLOGUE, newPrologue, newPrologue));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Publisher getPublisher() {
		if (publisher != null && publisher.eIsProxy()) {
			InternalEObject oldPublisher = (InternalEObject)publisher;
			publisher = (Publisher)eResolveProxy(oldPublisher);
			if (publisher != oldPublisher) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ComicshopPackage.COMIC__PUBLISHER, oldPublisher, publisher));
			}
		}
		return publisher;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Publisher basicGetPublisher() {
		return publisher;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setPublisher(Publisher newPublisher) {
		Publisher oldPublisher = publisher;
		publisher = newPublisher;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ComicshopPackage.COMIC__PUBLISHER, oldPublisher, publisher));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<Review> getReviews() {
		if (reviews == null) {
			reviews = new EObjectContainmentEList<Review>(Review.class, this, ComicshopPackage.COMIC__REVIEWS);
		}
		return reviews;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case ComicshopPackage.COMIC__PROLOGUE:
				return basicSetPrologue(null, msgs);
			case ComicshopPackage.COMIC__REVIEWS:
				return ((InternalEList<?>)getReviews()).basicRemove(otherEnd, msgs);
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
			case ComicshopPackage.COMIC__TITLE:
				return getTitle();
			case ComicshopPackage.COMIC__RELEASE_DATE:
				return getReleaseDate();
			case ComicshopPackage.COMIC__AUTHORS:
				return getAuthors();
			case ComicshopPackage.COMIC__PROLOGUE:
				return getPrologue();
			case ComicshopPackage.COMIC__PUBLISHER:
				if (resolve) return getPublisher();
				return basicGetPublisher();
			case ComicshopPackage.COMIC__REVIEWS:
				return getReviews();
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
			case ComicshopPackage.COMIC__TITLE:
				setTitle((String)newValue);
				return;
			case ComicshopPackage.COMIC__RELEASE_DATE:
				setReleaseDate((String)newValue);
				return;
			case ComicshopPackage.COMIC__AUTHORS:
				getAuthors().clear();
				getAuthors().addAll((Collection<? extends Author>)newValue);
				return;
			case ComicshopPackage.COMIC__PROLOGUE:
				setPrologue((Prologue)newValue);
				return;
			case ComicshopPackage.COMIC__PUBLISHER:
				setPublisher((Publisher)newValue);
				return;
			case ComicshopPackage.COMIC__REVIEWS:
				getReviews().clear();
				getReviews().addAll((Collection<? extends Review>)newValue);
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
			case ComicshopPackage.COMIC__TITLE:
				setTitle(TITLE_EDEFAULT);
				return;
			case ComicshopPackage.COMIC__RELEASE_DATE:
				setReleaseDate(RELEASE_DATE_EDEFAULT);
				return;
			case ComicshopPackage.COMIC__AUTHORS:
				getAuthors().clear();
				return;
			case ComicshopPackage.COMIC__PROLOGUE:
				setPrologue((Prologue)null);
				return;
			case ComicshopPackage.COMIC__PUBLISHER:
				setPublisher((Publisher)null);
				return;
			case ComicshopPackage.COMIC__REVIEWS:
				getReviews().clear();
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
			case ComicshopPackage.COMIC__TITLE:
				return TITLE_EDEFAULT == null ? title != null : !TITLE_EDEFAULT.equals(title);
			case ComicshopPackage.COMIC__RELEASE_DATE:
				return RELEASE_DATE_EDEFAULT == null ? releaseDate != null : !RELEASE_DATE_EDEFAULT.equals(releaseDate);
			case ComicshopPackage.COMIC__AUTHORS:
				return authors != null && !authors.isEmpty();
			case ComicshopPackage.COMIC__PROLOGUE:
				return prologue != null;
			case ComicshopPackage.COMIC__PUBLISHER:
				return publisher != null;
			case ComicshopPackage.COMIC__REVIEWS:
				return reviews != null && !reviews.isEmpty();
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
		result.append(" (title: ");
		result.append(title);
		result.append(", releaseDate: ");
		result.append(releaseDate);
		result.append(')');
		return result.toString();
	}

} //ComicImpl
