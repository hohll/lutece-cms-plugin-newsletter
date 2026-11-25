/**
 * The GrapesJS editor of the composition of a newsletter (grapesjs-preset-newsletter), in place of the rich text editor.
 *
 * The content of a newsletter is a list of table rows (<tr>), placed by the sending template inside the table of the email body. A browser
 * drops the rows that are not inside a table, so the editor shows them inside a locked table of the width of the email, and the save takes
 * them out again. What is dropped outside of the rows (a block of the preset, a text) becomes a row of its own.
 *
 * The editor is placed out of the form of the page: its modals hold buttons that would submit it. On submit, the hidden field of the form
 * receives the HTML of the editor, its CSS inlined (command gjs-get-inlined-html of the preset), without the identifiers GrapesJS generates to
 * attach its styles. The CSS of the canvas only (box-sizing, grey background, width of the email) is not exported.
 *
 * Nothing is loaded from outside the site: the telemetry of GrapesJS and its Font Awesome stylesheet (cssIcons, a CDN) are turned off.
 *
 * The buttons of the Lutece insert services (lutece-editor-insert.js of the core) insert their HTML into the selected cell. When the site
 * offers the image insert service (library), it replaces the asset manager of GrapesJS: the image block, a double-click on an image and the
 * image fields of the style manager take their image from it.
 *
 * The URLs of the content are made absolute against the URL of the site (data-base-url: the property lutece.prod.url of the core when
 * defined, the base URL of the request otherwise), for the inserted HTML as for the content loaded: the canvas of the editor has no base to resolve a relative URL, and an email
 * needs absolute ones.
 */
( function ( ) {
	'use strict';

	const ROWS_ATTRIBUTE = 'data-newsletter-rows';
	const INSERT_INPUT = 'grapesjs';
	const ICON_STYLE = 'style="display: block; max-width: 22px"';
	const GENERATED_ID = /^i[a-z0-9]{2,8}(-\d+)?$/;
	const URL_ATTRIBUTES = [ 'src', 'href', 'background', 'poster' ];
	const CELL_TAGS = [ 'td', 'th' ];
	const IMAGE_BUTTON = 'luteceImage';
	const KEPT_URL = /^([a-z][a-z0-9+.-]*:|\/\/|#|\$\{|\[)/i;

	/**
	 * Makes the relative URLs of an HTML fragment absolute
	 *
	 * @param {string} strHtml the fragment
	 * @param {string} strBaseUrl the URL of the site, the fragment is returned unchanged when empty
	 * @returns {string} the fragment, its URLs absolute
	 */
	function absoluteUrls( strHtml, strBaseUrl ) {
		if ( !strHtml || !strBaseUrl ) {
			return strHtml || '';
		}
		const template = document.createElement( 'template' );
		template.innerHTML = strHtml;
		let changed = false;
		URL_ATTRIBUTES.forEach( attribute => {
			template.content.querySelectorAll( `[${attribute}]` ).forEach( element => {
				const value = element.getAttribute( attribute ).trim( );
				if ( value && !KEPT_URL.test( value ) ) {
					try {
						element.setAttribute( attribute, new URL( value, strBaseUrl ).href );
						changed = true;
					} catch ( e ) {
						return;
					}
				}
			} );
		} );
		return changed ? template.innerHTML : strHtml;
	}
	const CANVAS_CSS = '* { box-sizing: border-box; } body { margin: 0; padding: 0; background-color: #f1f1f1; }'
		+ ' body > * { max-width: 600px; margin-left: auto; margin-right: auto; background-color: #ffffff; }';

	/**
	 * Wraps the rows of the content into the locked table of the email body
	 *
	 * @param {string} strHtml the content of the newsletter
	 * @returns {string} the content to edit
	 */
	function toEditable( strHtml ) {
		const template = document.createElement( 'template' );
		template.innerHTML = strHtml || '';
		const hasRows = Array.from( template.content.children ).some( element => element.tagName === 'TR' );
		if ( !hasRows ) {
			return strHtml || '';
		}
		return `<table ${ROWS_ATTRIBUTE}="true" role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%" style="width: 100%;"><tbody>${strHtml}</tbody></table>`;
	}

	/**
	 * Wraps an HTML fragment into a row of the email body
	 *
	 * @param {string} strHtml the fragment
	 * @returns {string} the row
	 */
	function toRow( strHtml ) {
		return `<tr><td>${strHtml}</td></tr>`;
	}

	/**
	 * Takes the rows out of the HTML of the editor: the rows of the locked table as they are, every other element or text as a row of its own
	 *
	 * @param {string} strHtml the HTML of the editor, its CSS inlined
	 * @returns {string} the content of the newsletter
	 */
	function toContent( strHtml ) {
		const template = document.createElement( 'template' );
		template.innerHTML = ( strHtml || '' ).trim( ).replace( /^<body[^>]*>/i, '' ).replace( /<\/body>$/i, '' );
		template.content.querySelectorAll( '[id]' ).forEach( element => {
			if ( GENERATED_ID.test( element.id ) ) {
				element.removeAttribute( 'id' );
			}
		} );
		const rows = [];
		template.content.childNodes.forEach( node => {
			if ( node.nodeType === Node.COMMENT_NODE ) {
				rows.push( `<!--${node.data}-->` );
			} else if ( node.nodeType === Node.TEXT_NODE ) {
				if ( node.textContent.trim( ) ) {
					rows.push( toRow( node.textContent ) );
				}
			} else if ( node.nodeType === Node.ELEMENT_NODE ) {
				if ( node.tagName === 'TABLE' && node.hasAttribute( ROWS_ATTRIBUTE ) ) {
					Array.from( node.rows ).forEach( row => rows.push( row.outerHTML ) );
				} else if ( node.tagName === 'TR' ) {
					rows.push( node.outerHTML );
				} else {
					rows.push( toRow( node.outerHTML ) );
				}
			}
		} );
		return rows.join( '\n' );
	}

	/**
	 * Locks the table of the email body: it cannot be moved, copied nor removed
	 *
	 * @param {Object} editor the GrapesJS editor
	 */
	function lockRowsTable( editor ) {
		editor.getWrapper( ).find( `[${ROWS_ATTRIBUTE}]` ).forEach( table => {
			table.set( { removable: false, draggable: false, copyable: false, selectable: false, hoverable: false } );
		} );
	}

	/**
	 * Inserts HTML into the selected cell: at its end when the cell itself is selected, after the selected component when it is inside a cell.
	 * Another selected component gets the HTML after it, and the HTML goes at the end of the content when nothing is selected.
	 *
	 * @param {Object} editor the GrapesJS editor
	 * @param {string} strHtml the HTML to insert
	 * @param {string} strBaseUrl the URL of the site, against which the relative URLs of the HTML are made absolute
	 */
	function insertHtml( editor, strHtml, strBaseUrl ) {
		strHtml = absoluteUrls( strHtml, strBaseUrl );
		const selected = editor.getSelected( );
		if ( !selected ) {
			editor.addComponents( strHtml );
			return;
		}
		if ( CELL_TAGS.includes( ( selected.get( 'tagName' ) || '' ).toLowerCase( ) ) ) {
			selected.append( strHtml );
			return;
		}
		const parent = selected.parent( );
		if ( parent ) {
			parent.components( ).add( strHtml, { at: selected.index( ) + 1 } );
		} else {
			editor.addComponents( strHtml );
		}
	}

	/**
	 * Returns the first image of an HTML fragment
	 *
	 * @param {string} strHtml the fragment
	 * @returns {{src: string, alt: string}|null} its source and alternative text, null when the fragment has no image
	 */
	function firstImage( strHtml ) {
		const template = document.createElement( 'template' );
		template.innerHTML = strHtml || '';
		const image = template.content.querySelector( 'img[src]' );
		return image ? { src: image.getAttribute( 'src' ), alt: image.getAttribute( 'alt' ) || '' } : null;
	}

	/**
	 * Replaces the asset manager of GrapesJS by the image insert service of the site: the image block when dropped, an image double-clicked and
	 * the image fields of the style manager open it, and the image chosen becomes the source of the target
	 *
	 * @param {Object} editor the GrapesJS editor
	 * @param {string} lang fr or en
	 * @param {Object} library the media types and spaces of the library insert service
	 * @param {string} strBaseUrl the URL of the site, against which the URL of the image is made absolute
	 */
	function useInsertServiceForImages( editor, lang, library, strBaseUrl ) {
		editor.Commands.add( 'open-assets', {
			run( ed, sender, options ) {
				const opts = options || {};
				const url = LuteceEditorInsert.serviceUrl( IMAGE_BUTTON, INSERT_INPUT, library, '' );
				LuteceEditorInsert.openInsertService( lang, IMAGE_BUTTON, url, html => {
					const image = firstImage( absoluteUrls( html, strBaseUrl ) );
					if ( !image ) {
						return;
					}
					const asset = ed.AssetManager.add( { src: image.src } );
					if ( typeof opts.select === 'function' ) {
						opts.select( asset, true );
					} else if ( opts.target ) {
						opts.target.set( 'src', image.src );
					}
					if ( image.alt && opts.target && typeof opts.target.addAttributes === 'function' ) {
						opts.target.addAttributes( { alt: image.alt } );
					}
				} );
			},
			stop( ) {
				return;
			}
		} );
	}

	/**
	 * Adds the buttons of the Lutece insert services the site offers to the options panel
	 *
	 * @param {Object} editor the GrapesJS editor
	 * @param {string} lang fr or en
	 * @param {string[]} allowed the insert services the page allows
	 * @param {Object} library the media types and spaces of the library insert service
	 * @param {string} strBaseUrl the URL of the site
	 */
	async function addInsertButtons( editor, lang, allowed, library, strBaseUrl ) {
		if ( typeof LuteceEditorInsert === 'undefined' ) {
			return;
		}
		const { buttons } = await LuteceEditorInsert.getAvailable( allowed );
		const labels = LuteceEditorInsert.labels( lang );
		if ( buttons.includes( IMAGE_BUTTON ) ) {
			useInsertServiceForImages( editor, lang, library, strBaseUrl );
		}
		buttons.filter( name => name !== 'luteceUploadPdf' ).forEach( name => {
			const command = `lutece-insert-${name}`;
			editor.Commands.add( command, {
				run( ed, sender ) {
					if ( sender && typeof sender.set === 'function' ) {
						sender.set( 'active', false );
					}
					const url = LuteceEditorInsert.serviceUrl( name, INSERT_INPUT, library, '' );
					LuteceEditorInsert.openInsertService( lang, name, url, html => insertHtml( ed, html, strBaseUrl ) );
				}
			} );
			editor.Panels.addButton( 'options', {
				id: command,
				label: `<span ${ICON_STYLE}>${LuteceEditorInsert.icon( name, labels[ name ] )}</span>`,
				command,
				attributes: { title: labels[ name ] || name }
			} );
		} );
	}

	/**
	 * Creates the editor of the page and binds it to the form
	 */
	function init( ) {
		const container = document.getElementById( 'newsletter-grapesjs' );
		const field = document.getElementById( 'html_content' );
		if ( !container || !field || typeof grapesjs === 'undefined' ) {
			return;
		}
		const form = field.form;
		const settings = container.dataset;
		const baseUrl = settings.baseUrl ? settings.baseUrl.replace( /\/?$/, '/' ) : '';
		const lang = ( document.documentElement.lang || 'en' ).toLowerCase( ).startsWith( 'fr' ) ? 'fr' : 'en';
		const i18n = { locale: lang, detectLocale: false };
		if ( lang === 'fr' && window.grapesjsLocaleFr ) {
			i18n.messagesAdd = { fr: window.grapesjsLocaleFr };
		}

		const presetOptions = {
			modalTitleImport: settings.labelImportTitle,
			modalLabelImport: settings.labelImport,
			modalBtnImport: settings.labelImportButton,
			modalTitleExport: settings.labelExportTitle,
			modalLabelExport: settings.labelExport,
			textCleanCanvas: settings.labelClearCanvas,
			cellStyle: {
				'font-family': 'sans-serif',
				'font-size': '15px',
				'line-height': '20px',
				color: '#555555',
				'vertical-align': 'top',
				margin: 0,
				padding: 0
			}
		};
		Object.keys( presetOptions ).forEach( key => presetOptions[ key ] === undefined && delete presetOptions[ key ] );
		const presetNewsletter = window[ 'grapesjs-preset-newsletter' ];
		const preset = presetNewsletter.default || presetNewsletter;

		const editor = grapesjs.init( {
			container,
			height: settings.height || '80vh',
			fromElement: false,
			components: toEditable( absoluteUrls( field.value, baseUrl ) ),
			storageManager: false,
			telemetry: false,
			cssIcons: '',
			selectorManager: { componentFirst: true },
			assetManager: { upload: false, assets: [] },
			protectedCss: '',
			canvasCss: CANVAS_CSS,
			i18n,
			plugins: [ ed => preset( ed, presetOptions ) ]
		} );

		editor.onReady( ( ) => {
			lockRowsTable( editor );
			const borders = editor.Panels.getButton( 'options', 'sw-visibility' );
			if ( borders ) {
				borders.set( 'active', true );
			}
			let library = {};
			try {
				library = JSON.parse( settings.library || '{}' );
			} catch ( e ) {
				library = {};
			}
			addInsertButtons( editor, lang, ( settings.insert || '' ).split( ',' ).map( s => s.trim( ) ).filter( Boolean ), library, baseUrl );
		} );

		form.addEventListener( 'submit', ( ) => {
			field.value = toContent( editor.runCommand( 'gjs-get-inlined-html' ) );
		} );
	}

	document.addEventListener( 'DOMContentLoaded', init );
} )( );
