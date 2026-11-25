import LuteceSearchList from '../../../../../shared/modules/luteceSearchList.js';

/**
 * Wires the choice of several newsletters rendered by the macro newsletterChoice: the filter of the list, the button selecting or
 * unselecting the newsletters shown, and the submit buttons of the form, enabled while at least one newsletter is checked.
 * @param {HTMLFormElement} form - The form holding the choice.
 */
export default function initNewsletterChoice( form ) {
	if ( !form ) { return; }
	const selectAll = form.querySelector( '.newsletter-choice-select-all' );
	const unselectAll = form.querySelector( '.newsletter-choice-unselect-all' );
	const items = form.querySelectorAll( '.newsletter-choice-item' );
	const submits = document.querySelectorAll( `[type="submit"][form="${form.id}"], #${form.id} [type="submit"]` );
	const checkboxOf = item => item.querySelector( 'input[name="newsletter_ids"]' );
	const visibleCheckboxes = () => Array.from( items ).filter( item => !item.classList.contains( 'd-none' ) ).map( checkboxOf );

	const update = () => {
		const visible = visibleCheckboxes();
		const anyChecked = Array.from( items ).some( item => checkboxOf( item ).checked );
		const allShownChecked = visible.length > 0 && visible.every( cb => cb.checked );
		selectAll.classList.toggle( 'd-none', allShownChecked );
		unselectAll.classList.toggle( 'd-none', !allShownChecked );
		selectAll.disabled = visible.length === 0;
		submits.forEach( btn => { btn.disabled = !anyChecked; } );
	};

	new LuteceSearchList( form.querySelector( '.newsletter-choice-search' ), items, {
		hideClass: 'd-none',
		extraSearchFunction: update
	} );
	selectAll.addEventListener( 'click', () => { visibleCheckboxes().forEach( cb => { cb.checked = true; } ); update(); } );
	unselectAll.addEventListener( 'click', () => { visibleCheckboxes().forEach( cb => { cb.checked = false; } ); update(); } );
	items.forEach( item => checkboxOf( item ).addEventListener( 'change', update ) );
	update();
}
